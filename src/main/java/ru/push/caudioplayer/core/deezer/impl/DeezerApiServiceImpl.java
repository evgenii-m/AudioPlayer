package ru.push.caudioplayer.core.deezer.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.deezer.DeezerApiAdapter;
import ru.push.caudioplayer.core.deezer.DeezerApiConst;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.DeezerNeedAuthorizationException;
import ru.push.caudioplayer.core.deezer.domain.Playlist;
import ru.push.caudioplayer.core.deezer.domain.Playlists;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.deezer.domain.Tracks;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.facades.domain.PlaylistType;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public String checkAuthorizationCode(String locationUri) throws DeezerNeedAuthorizationException {
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
				throw new DeezerNeedAuthorizationException("Error Reason: " + errorReason);
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
	public void getTrack(long trackId) throws DeezerNeedAuthorizationException {
		checkAccessToken();
		try {
			Track track = deezerApiAdapter.getTrack(trackId, currentAccessToken);
			LOG.debug("Received deezer track: {}", track);
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error:", e);
		}
	}

	@Override
	public List<PlaylistData> getPlaylists() throws DeezerNeedAuthorizationException {
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
			LOG.debug("Received deezer {} playlists: {}", playlists.size(), playlists);

			// get tracks for playlists
			return fetchPlaylistsTracks(playlists);
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error:", e);
			return new ArrayList<>();
		}
	}

	private List<PlaylistData> fetchPlaylistsTracks(List<Playlist> playlists) {
		List<PlaylistData> playlistData = new ArrayList<>();
		LOG.debug("Deezer fetching playlists tracks begin");

		List<Callable<ImmutablePair<Playlist, List<Track>>>> tasks = playlists.stream()
				.map(playlist -> (Callable<ImmutablePair<Playlist, List<Track>>>) () -> {
					List<Track> playlistTracks = new ArrayList<>();
					Tracks tracksResponse;
					int j = 0;
					do {
						try {
							tracksResponse = deezerApiAdapter.getPlaylistTracks(playlist.getId(), currentAccessToken, j, PLAYLISTS_DEFAULT_LIMIT);
							playlistTracks.addAll(tracksResponse.getData());
							j += PLAYLISTS_DEFAULT_LIMIT;
						} catch (DeezerApiErrorException e) {
							LOG.error("Deezer api error:", e);
							break;
						}
					} while (tracksResponse.getNext() != null);

//					LOG.debug("Received deezer playlist tracks: playlist = {}, size = {}, tracks = {}", playlist.getId(),
//							playlistTracks.size(), playlistTracks);
					LOG.debug("Received deezer playlist tracks: playlist = {}, size = {}", playlist.getId(), playlistTracks.size());

					return ImmutablePair.of(playlist, playlistTracks);
				})
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
				List<Track> entryTracks = entry.getRight();
				playlistData.add(
						new PlaylistData(String.valueOf(entryPlaylist.getId()), entryPlaylist.getTitle(),
								PlaylistType.DEEZER, entryPlaylist.getLink(),
								entryTracks.stream()
										.map(t -> new AudioTrackData.Builder(t.getPreview(), MediaSourceType.HTTP_STREAM, t.getArtist().getName(), t.getTitle())
												.album(t.getAlbum().getTitle())
												.length(t.getDuration() * 1000) // duration in seconds, length in milliseconds
												.build())
										.collect(Collectors.toList())
						)
				);
			}

		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Executor Service error", e);
		}

		LOG.debug("Deezer fetching playlists tracks end");

		return playlistData;
	}

	private void checkAccessToken() throws DeezerNeedAuthorizationException {
		if (currentAccessToken == null) {
			throw new DeezerNeedAuthorizationException("Access token not defined.");
		}
	}

}
