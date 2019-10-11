package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.DeezerPlaylistService;
import ru.push.caudioplayer.core.facades.DeezerLogicFacade;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackInfoData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.dto.TrackData;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.utils.DateTimeUtils;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Properties;

@Component("deezerLogicFacade")
public class DeezerLogicFacadeImpl extends AbstractPlaylistLogicFacadeImpl implements DeezerLogicFacade {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerLogicFacadeImpl.class);

	@Autowired
	private DeezerApiService deezerApiService;
	@Autowired
	private DeezerPlaylistService deezerPlaylistService;
	@Autowired
	private DtoMapper dtoMapper;
	@Autowired
	private Properties properties;

	private String monthlyPlaylistDateFormat;


	public DeezerLogicFacadeImpl() {
		super();
	}

	@Override
	protected PlaylistService getService() {
		return deezerPlaylistService;
	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());
		monthlyPlaylistDateFormat = properties.getProperty("deezer.application.monthly.playlist.title.format");
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
	public String getDeezerPlaylistWebPageUrl(String playlistUid) {
		Optional<Playlist> playlist = deezerPlaylistService.getPlaylist(playlistUid);
		if (playlist.isPresent() && playlist.get().isDeezer()) {
			return deezerApiService.getDeezerPlaylistWebPageUrl(Long.valueOf(playlistUid));
		} else {
			LOG.error("Incorrect playlist for open in web browser action: uid = {}", playlistUid);
			return null;
		}
	}


	@Override
	public void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackData lastFmTrackData) {
		TrackData trackData = new TrackData(lastFmTrackData.getArtist(), lastFmTrackData.getAlbum(),
				lastFmTrackData.getTitle());
		addLastFmTrackDeezerPlaylist(playlistUid, trackData);
	}

	@Override
	public void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(LastFmTrackData lastFmTrackData) {
		TrackData trackData = new TrackData(lastFmTrackData.getArtist(), lastFmTrackData.getAlbum(),
				lastFmTrackData.getTitle());
		addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(trackData);
	}

	@Override
	public void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackInfoData trackInfoData) {
		TrackData trackData = new TrackData(trackInfoData.getArtistName(), trackInfoData.getAlbumName(),
				trackInfoData.getTrackName());
		addLastFmTrackDeezerPlaylist(playlistUid, trackData);
	}

	@Override
	public void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(LastFmTrackInfoData trackInfoData) {
		TrackData trackData = new TrackData(trackInfoData.getArtistName(), trackInfoData.getAlbumName(),
				trackInfoData.getTrackName());
		addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(trackData);
	}

	private void addLastFmTrackDeezerPlaylist(String playlistUid, TrackData trackData) {
		Playlist result = deezerPlaylistService.addTrackToDeezerPlaylist(playlistUid, trackData);
		if (result != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_DEEZER_PLAYLIST_SUCCESS, result.getUid(), result.getTitle())
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.ADD_TRACKS_TO_DEEZER_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	private void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(TrackData trackData) {
		Playlist lovedTracksPlaylist = deezerPlaylistService.addTrackToDeezerFavoritesPlaylist(trackData);

		if (lovedTracksPlaylist != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(lovedTracksPlaylist)));
			sendNotification(NotificationMessages.ADD_TRACK_TO_DEEZER_LOVED_TRACKS_SUCCESS);
		} else {
			sendNotification(NotificationMessages.ADD_TRACK_TO_DEEZER_LOVED_TRACKS_FAIL);
		}

		String monthlyPlaylistName = DateTimeUtils.getCurrentTimestamp(monthlyPlaylistDateFormat);
		Playlist monthlyPlaylist;
		Optional<Playlist> deezerMonthlyPlaylistOptional = deezerPlaylistService.getPlaylistByTitle(monthlyPlaylistName).stream().findFirst();

		if (deezerMonthlyPlaylistOptional.isPresent()) {
			monthlyPlaylist = deezerMonthlyPlaylistOptional.get();
		} else {
			monthlyPlaylist = deezerPlaylistService.createPlaylist(monthlyPlaylistName);
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
			Playlist resultPlaylist = deezerPlaylistService.addTrackToDeezerPlaylist(monthlyPlaylist, trackData);
			if (resultPlaylist != null) {
				eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(resultPlaylist)));
				sendNotification(
						String.format(NotificationMessages.ADD_TRACKS_TO_DEEZER_PLAYLIST_SUCCESS, resultPlaylist.getUid(), resultPlaylist.getTitle())
				);
			} else {
				sendNotification(
						String.format(NotificationMessages.ADD_TRACKS_TO_DEEZER_PLAYLIST_FAIL_BY_TITLE, monthlyPlaylistName)
				);
			}
		}
	}

	@Override
	public PlaylistData getActivePlaylist() {
		return null;
	}
}
