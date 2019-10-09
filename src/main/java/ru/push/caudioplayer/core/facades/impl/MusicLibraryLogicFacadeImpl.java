package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackInfoData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.model.Track;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.playlist.LocalPlaylistService;
import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.model.Playlist;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component("musicLibraryLogicFacade")
public class MusicLibraryLogicFacadeImpl extends AbstractPlaylistLogicFacadeImpl implements MusicLibraryLogicFacade {

	private static final Logger LOG = LoggerFactory.getLogger(MusicLibraryLogicFacadeImpl.class);

	@Autowired
	private LocalPlaylistService localPlaylistService;
	@Autowired
	private LastFmService lastFmService;
	@Autowired
	private ApplicationConfigService applicationConfigService;
	@Autowired
	private DtoMapper dtoMapper;


	public MusicLibraryLogicFacadeImpl() {
		super();
	}

	@Override
	protected PlaylistService getService() {
		return localPlaylistService;
	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());
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
	public List<PlaylistData> getPlaylists() {
		return dtoMapper.mapPlaylistData(
				getService().getPlaylists().stream()
						.sorted(Comparator.comparingLong(Playlist::getPosition))
						.collect(Collectors.toList())
		);
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
	public LastFmTrackInfoData getLastFmTrackInfo(LastFmTrackData trackData) {
		TrackInfo trackInfo = lastFmService.getTrackInfo(trackData.getArtist(), trackData.getTitle());
		if (trackInfo != null) {
			return dtoMapper.mapLastFmTrackInfoData(trackInfo);
		} else {
			sendNotification(
					String.format(NotificationMessages.LASTFM_TRACK_INFO_NOT_FOUND, trackData.getArtist(), trackData.getTitle())
			);
			return null;
		}
	}

	@Override
	public PlaylistData getActivePlaylist() {
		return localPlaylistService.getActivePlaylist().map(o -> dtoMapper.mapPlaylistData(o)).orElse(null);
	}

	@Override
	public void addFilesToPlaylist(String playlistUid, List<File> files) {
		Playlist result = localPlaylistService.addFilesToLocalPlaylist(playlistUid, files);
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
	public void addLocationsToPlaylist(String playlistUid, List<String> locations) {
		Playlist result = localPlaylistService.addLocationsToLocalPlaylist(playlistUid, locations);
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
}
