package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.DeezerNeedAuthorizationException;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.domain.Track;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.domain.LastFmTrackData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class MusicLibraryLogicFacadeImpl implements MusicLibraryLogicFacade {

	private static final Logger LOG = LoggerFactory.getLogger(MusicLibraryLogicFacadeImpl.class);

	private final List<AudioPlayerEventListener> eventListeners;

	@Autowired
	private CustomPlaylistComponent playlistComponent;
	@Autowired
	private LastFmService lastFmService;
	@Autowired
	private DeezerApiService deezerApiService;
	@Autowired
	private ApplicationConfigService applicationConfigService;


	public MusicLibraryLogicFacadeImpl() {
		this.eventListeners = new ArrayList<>();
	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		refreshPlaylists();

		try {
			if (applicationConfigService.getDeezerAccessToken() != null) {
				getDeezerPlaylists();
			}
		} catch (DeezerNeedAuthorizationException e) {
			LOG.error("Deezer authorization fails: {}", e);
		}
	}

	@Override
	public synchronized void addEventListener(AudioPlayerEventListener listener) {
		eventListeners.add(listener);
	}

	@Override
	public synchronized void removeEventListener(AudioPlayerEventListener listener) {
		eventListeners.remove(listener);
	}

	@Override
	public void connectLastFm(Consumer<String> openAuthPageConsumer) {
		lastFmService.connectLastFm(openAuthPageConsumer);
	}

	@Override
	public String getDeezerUserAuthorizationPageUrl() {
		return deezerApiService.getUserAuthorizationPageUrl();
	}

	@Override
	public boolean processDeezerAuthorization(String redirectUri) {
		try {
			String authorizationCode = deezerApiService.checkAuthorizationCode(redirectUri);
			if (authorizationCode != null) {
				// request for access token by received authorization code
				String accessToken = deezerApiService.getAccessToken(authorizationCode);
				if (accessToken != null) {
					LOG.info("Deezer access token: {}", accessToken);
					// if access token received - authorization process ends
					return true;
				} else {
					LOG.error("Deezer access token is NULL");
				}
			}
		} catch (DeezerNeedAuthorizationException e) {
			LOG.error("Deezer authorization fails: {}", e);
			// if access error received - authorization process also ends
			return true;
		}

		// return false for continue checking
		return false;
	}

	@Override
	public List<LastFmTrackData> getRecentTracksFromLastFm() {
		List<Track> userRecentTracks = lastFmService.getUserRecentTracks();

		if (CollectionUtils.isEmpty(userRecentTracks)) {
			return new ArrayList<>();
		}

		return userRecentTracks.stream()
				.map(o -> new LastFmTrackData(o.getArtist().getName(), o.getAlbum().getName(), o.getName(), o.getNowPlaying(),
						((o.getDate() != null) && (o.getDate().getUts() != null)) ? new Date(o.getDate().getUts() * 1000) : null))
				.sorted((o1, o2) -> (o2.getScrobbleDate() != null) ? o2.getScrobbleDate().compareTo(o1.getScrobbleDate()) : 1)
				.collect(Collectors.toList());
	}


	@Override
	public void refreshPlaylists() {
		List<PlaylistData> playlists = applicationConfigService.getPlaylists();
		String activePlaylistUid = applicationConfigService.getActivePlaylistUid();
		String displayedPlaylistUid = applicationConfigService.getDisplayedPlaylistUid();

		boolean loadStatus = playlistComponent.loadPlaylists(playlists, activePlaylistUid, displayedPlaylistUid);

		if (!loadStatus) {
			PlaylistData newPlaylist = createNewPlaylist();
			applicationConfigService.saveAllPlaylists(
					playlistComponent.getPlaylists(),
					playlistComponent.getActivePlaylist(),
					playlistComponent.getDisplayedPlaylist()
			);
		}
	}

	@Override
	public List<PlaylistData> getPlaylists() {
		return playlistComponent.getPlaylists();
	}

	@Override
	public PlaylistData getActivePlaylist() {
		return playlistComponent.getActivePlaylist();
	}

	@Override
	public PlaylistData getDisplayedPlaylist() {
		return playlistComponent.getDisplayedPlaylist();
	}

	@Override
	public PlaylistData getPlaylist(String playlistUid) {
		return playlistComponent.getPlaylist(playlistUid);
	}

	@Override
	public PlaylistData showPlaylist(String playlistUid) {
		boolean displayedResult = playlistComponent.setDisplayedPlaylist(playlistUid);
		if (displayedResult) {
			applicationConfigService.saveDisplayedPlaylist(playlistComponent.getDisplayedPlaylist());
		}
		return playlistComponent.getDisplayedPlaylist();
	}

	@Override
	public PlaylistData showActivePlaylist() {
		PlaylistData activePlaylist = getActivePlaylist();
		playlistComponent.setDisplayedPlaylist(activePlaylist);
		applicationConfigService.saveDisplayedPlaylist(activePlaylist);
		return activePlaylist;
	}

	@Override
	public PlaylistData createNewPlaylist() {
		PlaylistData newPlaylist = playlistComponent.createNewPlaylist();
		eventListeners.forEach(listener -> listener.createdNewPlaylist(newPlaylist));
		applicationConfigService.savePlaylist(newPlaylist);
		return newPlaylist;
	}

	@Override
	public boolean deletePlaylist(String playlistUid) {
		PlaylistData requiredPlaylist = getPlaylist(playlistUid);
		boolean deleteDisplayed = playlistComponent.getDisplayedPlaylist().equals(requiredPlaylist);

		if (playlistComponent.getPlaylists().size() == 1) {
			PlaylistData newPlaylist = playlistComponent.createNewPlaylist();
			applicationConfigService.savePlaylist(newPlaylist);
		}

		boolean deleteResult = playlistComponent.deletePlaylist(playlistUid);
		if (deleteResult) {
			if (deleteDisplayed) {
				eventListeners.forEach(listener -> listener.changedPlaylist(playlistComponent.getDisplayedPlaylist()));
			}
			applicationConfigService.deletePlaylist(requiredPlaylist);
		}
		return deleteResult;
	}

	@Override
	public void renamePlaylist(String playlistUid, String newPlaylistName) {
		PlaylistData changedPlaylist = playlistComponent.renamePlaylist(playlistUid, newPlaylistName);
		applicationConfigService.renamePlaylist(changedPlaylist);
		eventListeners.forEach(listener -> listener.renamedPlaylist(changedPlaylist));
	}

	@Override
	public void addFilesToPlaylist(List<File> files) {
		PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
		PlaylistData changedPlaylist = playlistComponent.addFilesToPlaylist(displayedPlaylist.getUid(), files);
		applicationConfigService.savePlaylist(changedPlaylist);
		eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
	}

	@Override
	public void deleteItemsFromPlaylist(List<Integer> itemsIndexes) {
		PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
		PlaylistData changedPlaylist = playlistComponent.deleteItemsFromPlaylist(displayedPlaylist.getUid(), itemsIndexes);
		applicationConfigService.savePlaylist(changedPlaylist);
		eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
	}

	@Override
	public void addLocationsToPlaylist(List<String> locations) {
		PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
		PlaylistData changedPlaylist = playlistComponent.addLocationsToPlaylist(displayedPlaylist.getUid(), locations);
		applicationConfigService.savePlaylist(changedPlaylist);
		eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
	}

	@Override
	public void getTrackFromDeezer(int trackId) throws DeezerNeedAuthorizationException {
		deezerApiService.getTrack(trackId);
	}

	@Override
	public List<PlaylistData> getDeezerPlaylists() throws DeezerNeedAuthorizationException {
		return deezerApiService.getPlaylists();
	}
}
