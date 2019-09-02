package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.converter.ImportExportConverter;
import ru.push.caudioplayer.core.converter.domain.PlaylistExportData;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.DeezerNeedAuthorizationException;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.facades.domain.PlaylistType;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.domain.Track;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.domain.LastFmTrackData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	@Autowired
	private ImportExportConverter importExportConverter;


	public MusicLibraryLogicFacadeImpl() {
		this.eventListeners = new ArrayList<>();
	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		refreshPlaylists();
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
		List<PlaylistData> playlists = new ArrayList<>();
		try {
			List<PlaylistData> deezerPlaylists = deezerApiService.getPlaylists();
			playlists.addAll(deezerPlaylists);
		} catch (DeezerNeedAuthorizationException e) {
			LOG.warn("Deezer playlist not loaded", e);
		}
		List<PlaylistData> localPlaylists = applicationConfigService.getPlaylists();
		playlists.addAll(localPlaylists);

		String activePlaylistUid = applicationConfigService.getActivePlaylistUid();
		String displayedPlaylistUid = applicationConfigService.getDisplayedPlaylistUid();

		boolean loadStatus = playlistComponent.loadPlaylists(playlists, activePlaylistUid, displayedPlaylistUid);

		if (!loadStatus) {
			PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
			eventListeners.forEach(listener -> listener.createdNewPlaylist(displayedPlaylist));
			applicationConfigService.savePlaylist(displayedPlaylist);
		}
		applicationConfigService.saveDisplayedPlaylist(playlistComponent.getDisplayedPlaylist());
		applicationConfigService.saveActivePlaylist(playlistComponent.getActivePlaylist());
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
		if (PlaylistType.LOCAL.equals(getDisplayedPlaylist().getPlaylistType())) {
			PlaylistData  newPlaylist = playlistComponent.createNewPlaylist(PlaylistType.LOCAL);
			applicationConfigService.savePlaylist(newPlaylist);
			eventListeners.forEach(listener -> listener.createdNewPlaylist(newPlaylist));
			return newPlaylist;

		} else if (PlaylistType.DEEZER.equals(getDisplayedPlaylist().getPlaylistType())) {
			 PlaylistData newPlaylist = playlistComponent.createNewPlaylist(PlaylistType.DEEZER);
			try {
				Long playlistUid = deezerApiService.createPlaylist(newPlaylist.getName());
				newPlaylist.setUid(String.valueOf(playlistUid));
				eventListeners.forEach(listener -> listener.createdNewPlaylist(newPlaylist));
				return newPlaylist;
			} catch (DeezerApiErrorException | DeezerNeedAuthorizationException e) {
				LOG.error("Create Deezer playlist error:", e);
			}
		}
		return null;
	}

	@Override
	public boolean deletePlaylist(String playlistUid) {
		boolean deleteDisplayed = playlistComponent.getDisplayedPlaylist().getUid().equals(playlistUid);
		boolean deleteResult = playlistComponent.deletePlaylist(playlistUid);

		if (deleteResult) {
			PlaylistData requiredPlaylist = getPlaylist(playlistUid);
			if (PlaylistType.LOCAL.equals(requiredPlaylist.getPlaylistType())) {
				if (playlistComponent.getLocalPlaylistsCount() == 0) {
					PlaylistData newPlaylist = playlistComponent.createNewPlaylist(PlaylistType.LOCAL);
					applicationConfigService.savePlaylist(newPlaylist);
				}
			}
			if (deleteDisplayed) {
				eventListeners.forEach(listener -> listener.changedPlaylist(playlistComponent.getDisplayedPlaylist()));
			}

			if (PlaylistType.LOCAL.equals(requiredPlaylist.getPlaylistType())) {
				applicationConfigService.deletePlaylist(requiredPlaylist);
			} else if (PlaylistType.DEEZER.equals(requiredPlaylist.getPlaylistType())) {
				try {
					deleteResult = deezerApiService.deletePlaylist(Long.valueOf(requiredPlaylist.getUid()));
				} catch (DeezerNeedAuthorizationException | DeezerApiErrorException e) {
					LOG.error("Delete Deezer playlist failed: id = {}, error = {}", playlistUid, e);
					deleteResult = false;
				}
			}
		}
		return deleteResult;
	}

	@Override
	public boolean renamePlaylist(String playlistUid, String newPlaylistName) {
		PlaylistData changedPlaylist = playlistComponent.renamePlaylist(playlistUid, newPlaylistName);
		if (changedPlaylist == null) {
			return false;
		}

		if (PlaylistType.LOCAL.equals(changedPlaylist.getPlaylistType())) {
			applicationConfigService.renamePlaylist(changedPlaylist);
		} else if (PlaylistType.DEEZER.equals(changedPlaylist.getPlaylistType())) {
			try {
				deezerApiService.renamePlaylist(changedPlaylist);
			} catch (DeezerNeedAuthorizationException | DeezerApiErrorException e) {
				LOG.error("Rename Deezer playlist error: ", e);
				return false;
			}
		} else {
			return false;
		}

		eventListeners.forEach(listener -> listener.renamedPlaylist(changedPlaylist));
		return true;
	}

	@Override
	public void exportPlaylistToFile(String playlistUid, String folderPath) throws JAXBException {
		PlaylistData playlist = playlistComponent.getPlaylist(playlistUid);
		File file = new File(folderPath + playlist.getExportFileName());
		PlaylistExportData exportData = importExportConverter.toPlaylistExportData(playlist);
		XmlUtils.marshalDocument(exportData, file, PlaylistExportData.class.getPackage().getName());
		LOG.info("export playlist to file: uid = {}, filePath = {}", playlistUid, file.getAbsolutePath());
	}

	@Override
	public void backupPlaylists(String folderName) {
		List<PlaylistData> playlists = playlistComponent.getPlaylists();

		playlists.forEach(playlist -> {
			PlaylistExportData exportData = importExportConverter.toPlaylistExportData(playlist);
			File file = new File(folderName + playlist.getExportFileName());
			try {
				XmlUtils.marshalDocument(exportData, file, PlaylistExportData.class.getPackage().getName());
				LOG.info("export playlist to file: uid = {}, filePath = {}", playlist.getUid(), file.getAbsolutePath());
			} catch (JAXBException e) {
				LOG.error("Playlist backup saving error: playlist = {} {}, error = {}",
						playlist.getUid(), playlist.getName(), e);
			}
		});
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
		List<PlaylistData> deezerPlaylists = deezerApiService.getPlaylists();
		playlistComponent.appendOrUpdatePlaylists(deezerPlaylists);
		return deezerPlaylists;
	}

	@Override
	public boolean addLastFmTrackToCurrentDeezerPlaylist(LastFmTrackData trackData) {
		PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
		if (!PlaylistType.DEEZER.equals(displayedPlaylist.getPlaylistType())) {
			LOG.error("Operation supported only Deezer platlists");
			return false;
		}

		String query = trackData.getArtist() + " " + trackData.getTitle();
		if (trackData.getAlbum() != null) {
			query = query + " " + trackData.getAlbum();
		}

		try {
			List<AudioTrackData> searchedTracksResult = deezerApiService.searchTracksQuery(query);
			LOG.info("Searched {} tracks: {}", searchedTracksResult.size(), searchedTracksResult);
			if (!CollectionUtils.isEmpty(searchedTracksResult)) {
				AudioTrackData searchedTrack = searchedTracksResult.get(0);
				String trackId = searchedTrack.getTrackId();
				boolean result = deezerApiService.addTrackToPlaylist(Long.valueOf(displayedPlaylist.getUid()), Long.valueOf(trackId));
				if (result) {
					playlistComponent.addAudioTrackToPlaylist(displayedPlaylist.getUid(), searchedTrack);
					eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
				}
				return result;
			}
		} catch (DeezerNeedAuthorizationException | DeezerApiErrorException e) {
			LOG.error("Add track to Deezer error: track data = {}, error = {}", trackData, e);
		}
		return false;
	}
}
