package ru.push.caudioplayer.core.deezer.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.converter.ImportExportConverter;
import ru.push.caudioplayer.core.deezer.DeezerApiAdapter;
import ru.push.caudioplayer.core.deezer.DeezerApiConst;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.domain.Playlist;
import ru.push.caudioplayer.core.deezer.domain.Playlists;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.deezer.domain.Tracks;
import ru.push.caudioplayer.core.deezer.domain.internal.PlaylistId;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DeezerApiServiceImpl implements DeezerApiService {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiServiceImpl.class);

	private static final Integer PLAYLISTS_DEFAULT_LIMIT = 500;

	@Autowired
	private Properties properties;

	@Autowired
	private DeezerApiAdapter deezerApiAdapter;

	@Autowired
	private ApplicationConfigService applicationConfigService;


	private ExecutorService executorService;

	private String currentAccessToken;


	public DeezerApiServiceImpl() {
	}

	@PostConstruct
	public void init() {
		String deezerApiServiceThreadPoolSize = properties.getProperty("deezer.api.service.thread.pool.size");
		executorService = Executors.newFixedThreadPool(Integer.valueOf(deezerApiServiceThreadPoolSize));

		String accessToken = applicationConfigService.getDeezerAccessToken();
		if (accessToken != null) {
			LOG.info("Load Deezer access token from configuration: {}", accessToken);
			this.currentAccessToken = accessToken;
		}
	}

	@PreDestroy
	public void stop() {
		executorService.shutdown();
	}

	@Override
	public String getUserAuthorizationPageUrl() {
		return deezerApiAdapter.getUserAuthorizationPageUrl();
	}

	@Override
	public String checkAuthorizationCode(String locationUri) throws DeezerApiErrorException {
		assert locationUri != null;

		if (locationUri.startsWith(DeezerApiConst.DEEZER_API_DEFAULT_REDIRECT_URI)) {
			LOG.debug("redirect location url detected: {}", locationUri);

			int codeParamStartPosition = locationUri.indexOf(DeezerApiConst.DEEZER_API_AUTH_PARAM_CODE_NAME);
			if (codeParamStartPosition > 0) {
				// append param name length and 1 for character '=' to make substring only for value
				String deezerAppAuthCode = locationUri.substring(
						codeParamStartPosition + DeezerApiConst.DEEZER_API_AUTH_PARAM_CODE_NAME.length() + 1);
				LOG.info("Deezer authorization code: {}", deezerAppAuthCode);
				return deezerAppAuthCode;
			}

			int errorReasonParamStartPosition = locationUri.indexOf(DeezerApiConst.DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME);
			if (errorReasonParamStartPosition > 0) {
				String errorReason = locationUri.substring(
						errorReasonParamStartPosition + DeezerApiConst.DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME.length() + 1);
				throw new DeezerApiErrorException("Error Reason: " + errorReason);
			}
		}

		return null;
	}

	@Override
	public String getAccessToken(String code) {
		String accessToken = applicationConfigService.getDeezerAccessToken();
		if (accessToken != null) {
			LOG.warn("Deezer access token already set in configuration, they will be overwritten: access token = {}", accessToken);
		}

		String newAccessToken = deezerApiAdapter.getAccessToken(code);
		if (newAccessToken != null) {
			LOG.info("Set new Deezer access token: {}", newAccessToken);
			currentAccessToken = newAccessToken;
			applicationConfigService.saveDeezerAccessToken(currentAccessToken);
		} else {
			LOG.warn("Received empty access token, current user token will not be updated");
		}

		return currentAccessToken;
	}

	@Override
	public void getTrack(long trackId) throws DeezerApiErrorException {
		checkAccessToken();
		try {
			Track track = deezerApiAdapter.getTrack(trackId, currentAccessToken);
			LOG.debug("Received deezer track: {}", track);
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error:", e);
		}
	}

	@Override
	public List<Playlist> getPlaylists() throws DeezerApiErrorException {
		checkAccessToken();
		try {
			// get user playlists
			List<Playlist> playlists = new ArrayList<>();
			Playlists playlistsResponse;
			int index = 0;
			do {
				playlistsResponse = deezerApiAdapter.getPlaylists(currentAccessToken, index, PLAYLISTS_DEFAULT_LIMIT);
				playlists.addAll(playlistsResponse.getData());
				index += PLAYLISTS_DEFAULT_LIMIT;
			} while (playlistsResponse.getNext() != null);
			LOG.info("Received deezer {} playlists: {}", playlists.size(), playlists);

			// get tracks for playlists
			fetchPlaylistsTracks(playlists);
			return playlists;

		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error:", e);
			return new ArrayList<>();
		}
	}

	private void fetchPlaylistsTracks(List<Playlist> playlists) {
		List<ru.push.caudioplayer.core.playlist.domain.Playlist> playlistData = new ArrayList<>();
		LOG.debug("Deezer fetching playlists tracks begin");

		List<Callable<ImmutablePair<Playlist, List<Track>>>> tasks = playlists.stream()
				.map(playlist -> (Callable<ImmutablePair<Playlist, List<Track>>>) () ->
						ImmutablePair.of(playlist, getPlaylistAllTracks(playlist.getId())))
				.collect(Collectors.toList());

		try {
			List<Future<ImmutablePair<Playlist, List<Track>>>> futures = executorService.invokeAll(tasks);

			// wait until all task will be executed
			while (futures.stream().anyMatch(future -> !future.isDone() && !future.isCancelled())) {
				TimeUnit.MILLISECONDS.sleep(100);
			}

			for (Future<ImmutablePair<Playlist, List<Track>>> future : futures) {
				ImmutablePair<Playlist, List<Track>> entry = future.get();
				Playlist entryPlaylist = entry.getLeft();
				Tracks entryTracks = new Tracks();
				entryTracks.setData(entry.getRight());
				entryPlaylist.setTracks(entryTracks);
			}

		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Executor Service error", e);
		}

		LOG.debug("Deezer fetching playlists tracks end");
	}

	private List<Track> getPlaylistAllTracks(long playlistId) {
		List<Track> playlistTracks = new ArrayList<>();
		Tracks tracksResponse;
		int j = 0;
		do {
			try {
				tracksResponse = deezerApiAdapter.getPlaylistTracks(playlistId, currentAccessToken, j, PLAYLISTS_DEFAULT_LIMIT);
				playlistTracks.addAll(tracksResponse.getData());
				j += PLAYLISTS_DEFAULT_LIMIT;
			} catch (DeezerApiErrorException e) {
				LOG.error("Deezer api error:", e);
				break;
			}
		} while (tracksResponse.getNext() != null);

		LOG.info("Received deezer playlist tracks: playlist = {}, size = {}", playlistId, playlistTracks.size());
		return playlistTracks;
	}

	@Override
	public List<Track> getPlaylistTracks(long playlistId) throws DeezerApiErrorException {
		checkAccessToken();
		return getPlaylistAllTracks(playlistId);
	}

	@Override
	public Long createPlaylist(String title) throws DeezerApiErrorException {
		checkAccessToken();
		PlaylistId playlistId = deezerApiAdapter.createPlaylist(title, currentAccessToken);
		return playlistId.getId();
	}

	@Override
	public boolean deletePlaylist(long playlistId) throws DeezerApiErrorException {
		checkAccessToken();
		return deezerApiAdapter.deletePlaylist(playlistId, currentAccessToken);
	}

	@Override
	public boolean renamePlaylist(long playlistId, String newTitle) throws DeezerApiErrorException {
		checkAccessToken();
		return deezerApiAdapter.renamePlaylist(playlistId, newTitle, currentAccessToken);
	}

	@Override
	public boolean addTrackToPlaylist(long playlistId, long trackId) throws DeezerApiErrorException {
		return addTracksToPlaylist(playlistId, Collections.singletonList(trackId));
	}

	@Override
	public boolean addTracksToPlaylist(long playlistId, List<Long> trackIds) throws DeezerApiErrorException {
		checkAccessToken();
		return deezerApiAdapter.addTracksToPlaylist(playlistId, trackIds, currentAccessToken);
	}

	@Override
	public boolean removeTrackFromPlaylist(long playlistId, long trackId) throws DeezerApiErrorException {
		return removeTracksFromPlaylist(playlistId, Collections.singletonList(trackId));
	}

	@Override
	public boolean removeTracksFromPlaylist(long playlistId, List<Long> trackIds) throws DeezerApiErrorException {
		checkAccessToken();
		return deezerApiAdapter.removeTracksFromPlaylist(playlistId, trackIds, currentAccessToken);
	}

	@Override
	public List<Track> searchTracksQuery(String shortQuery, String extendedQuery) throws DeezerApiErrorException {
		checkAccessToken();

		Tracks tracksResult = null;
		if (extendedQuery != null) {
			tracksResult = deezerApiAdapter.searchTracksQuery(extendedQuery, currentAccessToken);
		}
		if ((tracksResult == null) || CollectionUtils.isEmpty(tracksResult.getData())) {
			tracksResult = deezerApiAdapter.searchTracksQuery(shortQuery, currentAccessToken);
		}

		if ((tracksResult != null) && !CollectionUtils.isEmpty(tracksResult.getData())) {
			return tracksResult.getData();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public boolean addTrackToFavorites(long trackId) throws DeezerApiErrorException {
		checkAccessToken();
		return deezerApiAdapter.addTrackToFavorites(trackId, currentAccessToken);
	}

	@Override
	public boolean removeTrackFromFavorites(long trackId) throws DeezerApiErrorException {
		checkAccessToken();
		return deezerApiAdapter.removeTrackFromFavorites(trackId, currentAccessToken);
	}

	private void checkAccessToken() throws DeezerApiErrorException {
		if (currentAccessToken == null) {
			throw new DeezerApiErrorException("Access token not defined.");
		}
	}

}
