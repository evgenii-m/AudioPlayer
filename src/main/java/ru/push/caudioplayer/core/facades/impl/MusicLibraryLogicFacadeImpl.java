package ru.push.caudioplayer.core.facades.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.NotificationData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.model.Track;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistType;
import ru.push.caudioplayer.core.playlist.dto.TrackData;
import ru.push.caudioplayer.utils.DateTimeUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
public class MusicLibraryLogicFacadeImpl implements MusicLibraryLogicFacade {

	private static final Logger LOG = LoggerFactory.getLogger(MusicLibraryLogicFacadeImpl.class);

	private final List<AudioPlayerEventListener> eventListeners;

	@Autowired
	private PlaylistService playlistService;
	@Autowired
	private LastFmService lastFmService;
	@Autowired
	private DeezerApiService deezerApiService;
	@Autowired
	private ApplicationConfigService applicationConfigService;
	@Autowired
	private DtoMapper dtoMapper;
	@Autowired
	private Properties properties;

	private String monthlyPlaylistDateFormat;


	public MusicLibraryLogicFacadeImpl() {
		this.eventListeners = new ArrayList<>();
	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());
		monthlyPlaylistDateFormat = properties.getProperty("deezer.application.monthly.playlist.title.format");
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
	public String getLastFmToken() {
		return lastFmService.getToken();
	}

	@Override
	public String getLastFmAuthorizationPageUrl(String token) {
		return lastFmService.getUserAuthorizationPageUrl(token);
	}

	@Override
	public boolean processLastFmAuthorization(String token, String pageUrl) {
		boolean result = lastFmService.setSessionByToken(token, pageUrl);
		if (result) {
			sendNotification(NotificationMessages.LAST_FM_AUTHORIZATION_SUCCESS);
		} else {
			sendNotification(NotificationMessages.LAST_FM_AUTHORIZATION_FAIL);
		}
		return result;
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
					sendNotification(NotificationMessages.DEEZER_AUTHORIZATION_SUCCESS);
				} else {
					LOG.error("Deezer access token is NULL");
					sendNotification(NotificationMessages.DEEZER_AUTHORIZATION_FAIL);
				}
				return true;
			}
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer authorization fails: {}", e);
			sendNotification(NotificationMessages.DEEZER_AUTHORIZATION_FAIL);
			// if access error received - authorization process also ends
			return true;
		}

		// return false for continue checking
		return false;
	}

	@Override
	public List<LastFmTrackData> getRecentTracksFromLastFm(boolean fetchMore) {
		List<Track> userRecentTracks = lastFmService.getUserRecentTracks(fetchMore);

		if (CollectionUtils.isEmpty(userRecentTracks)) {
			return new ArrayList<>();
		}

		return dtoMapper.mapLastFmTrackData(userRecentTracks).stream()
				.sorted((o1, o2) -> (o2.getScrobbleDate() != null) ? o2.getScrobbleDate().compareTo(o1.getScrobbleDate()) : 1)
				.collect(Collectors.toList());
	}

	@Override
	public void reloadPlaylists() {
		playlistService.reloadPlaylists();
	}

	@Override
	public List<PlaylistData> getLocalPlaylists() {
		return dtoMapper.mapPlaylistData(
				playlistService.getPlaylists().stream()
						.filter(Playlist::isLocal)
						.sorted(Comparator.comparingLong(Playlist::getPosition))
						.collect(Collectors.toList())
		);
	}

	@Override
	public List<PlaylistData> getDeezerPlaylists() {
		return dtoMapper.mapPlaylistData(
				playlistService.getPlaylists().stream()
						.filter(Playlist::isDeezer)
						.collect(Collectors.toList())
		);
	}

	@Override
	public Optional<PlaylistData> getActivePlaylist() {
		return playlistService.getActivePlaylist().map(o -> dtoMapper.mapPlaylistData(o));
	}

	@Override
	public void createLocalPlaylist() {
		Playlist result = playlistService.createPlaylist(PlaylistType.LOCAL);
		if (result != null) {
			eventListeners.forEach(l -> l.createdNewPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.CREATE_PLAYLIST_SUCCESS, result.getUid(), result.getTitle())
			);
		} else {
			sendNotification(NotificationMessages.CREATE_PLAYLIST_FAIL);
		}
	}

	@Override
	public void createDeezerPlaylist() {
		Playlist result = playlistService.createPlaylist(PlaylistType.DEEZER);
		if (result != null) {
			eventListeners.forEach(l -> l.createdNewPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.CREATE_PLAYLIST_SUCCESS, result.getUid(), result.getTitle())
			);
		} else {
			sendNotification(NotificationMessages.CREATE_PLAYLIST_FAIL);
		}
	}

	@Override
	public void deletePlaylist(String playlistUid) {
		Playlist result = playlistService.deletePlaylist(playlistUid);
		if (result != null) {
			eventListeners.forEach(l -> l.deletedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.DELETE_PLAYLIST_SUCCESS, result.getUid())
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.DELETE_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	@Override
	public void renamePlaylist(String playlistUid, String newTitle) {
		Playlist result = playlistService.renamePlaylist(playlistUid, newTitle);
		if (result != null) {
			eventListeners.forEach(l -> l.renamedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.RENAME_PLAYLIST_SUCCESS, playlistUid, newTitle)
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.RENAME_PLAYLIST_FAIL, playlistUid, newTitle)
			);
		}
	}

	@Override
	public void backupPlaylists(String folderName) {
		List<Playlist> playlists = playlistService.getPlaylists();
		playlists.forEach(p -> playlistService.exportPlaylistToFile(p.getUid(), folderName));
		LOG.info("Playlists backups to folder '{}'", folderName);
	}

	@Override
	public void exportPlaylistToFile(String playlistUid, String folderPath) {
		playlistService.exportPlaylistToFile(playlistUid, folderPath);
	}

	@Override
	public void addFilesToPlaylist(String playlistUid, List<File> files) {
		Playlist result = playlistService.addFilesToLocalPlaylist(playlistUid, files);
		if (result != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_SUCCESS, result.getUid(), result.getTitle())
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	@Override
	public void deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid) {
		Playlist result = playlistService.deleteItemsFromPlaylist(playlistUid, tracksUid);
		if (result != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.DELETE_TRACKS_FROM_PLAYLIST_SUCCESS, playlistUid)
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.DELETE_TRACKS_FROM_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	@Override
	public void addLocationsToPlaylist(String playlistUid, List<String> locations) {
		Playlist result = playlistService.addLocationsToLocalPlaylist(playlistUid, locations);
		if (result != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_SUCCESS, result.getUid(), result.getTitle())
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	@Override
	public void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackData trackData) {
		Playlist result = playlistService.addTrackToDeezerPlaylist(playlistUid,
				new TrackData(trackData.getArtist(), trackData.getAlbum(), trackData.getTitle()));
		if (result != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_SUCCESS, result.getUid(), result.getTitle())
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	@Override
	public void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(LastFmTrackData lastFmTrackData) {
		TrackData trackData = new TrackData(lastFmTrackData.getArtist(), lastFmTrackData.getAlbum(), lastFmTrackData.getTitle());
		Playlist lovedTracksPlaylist = playlistService.addTrackToDeezerFavoritesPlaylist(trackData);

		if (lovedTracksPlaylist != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(lovedTracksPlaylist)));
			sendNotification(NotificationMessages.ADD_TRACK_TO_DEEZER_LOVED_TRACKS_SUCCESS);
		} else {
			sendNotification(NotificationMessages.ADD_TRACK_TO_DEEZER_LOVED_TRACKS_FAIL);
		}

		String monthlyPlaylistName = DateTimeUtils.getCurrentTimestamp(monthlyPlaylistDateFormat);
		Playlist monthlyPlaylist;
		Optional<Playlist> deezerMonthlyPlaylistOptional = playlistService.getPlaylist(PlaylistType.DEEZER, monthlyPlaylistName);

		if (deezerMonthlyPlaylistOptional.isPresent()) {
			monthlyPlaylist = deezerMonthlyPlaylistOptional.get();
		} else {
			monthlyPlaylist = playlistService.createPlaylist(PlaylistType.DEEZER, monthlyPlaylistName);
			if (monthlyPlaylist != null) {
				eventListeners.forEach(listener -> listener.createdNewPlaylist(dtoMapper.mapPlaylistData(monthlyPlaylist)));
				sendNotification(
						String.format(NotificationMessages.CREATE_PLAYLIST_SUCCESS, monthlyPlaylist.getUid(), monthlyPlaylist.getTitle())
				);
				LOG.info("Monthly playlist not found, create new: {}", monthlyPlaylist);
			} else {
				sendNotification(NotificationMessages.CREATE_PLAYLIST_FAIL);
			}
		}

		if (monthlyPlaylist != null) {
			Playlist resultPlaylist = playlistService.addTrackToDeezerPlaylist(monthlyPlaylist.getUid(), trackData);
			if (resultPlaylist != null) {
				eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(resultPlaylist)));
				sendNotification(
						String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_SUCCESS, resultPlaylist.getUid(), resultPlaylist.getTitle())
				);
			} else {
				sendNotification(
						String.format(NotificationMessages.ADD_TRACKS_TO_PLAYLIST_FAIL_BY_TITLE, monthlyPlaylistName)
				);
			}
		}
	}

	private void sendNotification(String messageText) {
		eventListeners.forEach(l -> l.obtainedNotification(new NotificationData(messageText)));
	}
}
