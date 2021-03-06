package ru.push.caudioplayer.core.lastfm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.lastfm.LastFmApiProvider;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.lastfm.model.RecentTracks;
import ru.push.caudioplayer.core.lastfm.model.ScrobblesResult;
import ru.push.caudioplayer.core.lastfm.model.Track;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;
import ru.push.caudioplayer.core.lastfm.model.UpdateNowPlayingResult;
import ru.push.caudioplayer.core.playlist.model.MediaSourceType;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
@Component
public class DefaultLastFmService implements LastFmService {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultLastFmService.class);

  private static final int RECENT_TRACKS_INITIAL_PAGE_SIZE = 10;
	private static final int RECENT_TRACKS_PAGE_STEP = 6;
	private static final long SCROBBLE_MAX_DELAY_MS = 4 * 60 * 1000; 	// 4 minutes
	private static final long SCROBBLE_DEFAULT_DELAY_MS = 2 * 60 * 1000; // 2 minutes

  @Autowired
	private LastFmApiProvider apiAdapter;
	@Autowired
	private ApplicationConfigService applicationConfigService;

	private LastFmSessionData currentSessionData;
	private int currentRecentTracksPageSize;


	@PostConstruct
	public void init() {
		LastFmSessionData sessionData = applicationConfigService.getLastFmSessionData();
		if (sessionData != null) {
			LOG.info("Load Last.fm session from configuration: username = {}, session key = {}",
					sessionData.getUsername(), sessionData.getSessionKey());
			currentSessionData = sessionData;
		}
		currentRecentTracksPageSize = RECENT_TRACKS_INITIAL_PAGE_SIZE;
	}

	/**
	 * See https://www.last.fm/api/desktopauth
	 */
	public String getToken() {
		LastFmSessionData sessionData = applicationConfigService.getLastFmSessionData();
		if (sessionData != null) {
			LOG.warn("Last.fm user data already set in configuration, they will be overwritten: username = {}, session key = {}",
					sessionData.getUsername(), sessionData.getSessionKey());
		}

		// 1. API Key into adapter

		// 2. Fetch a request token
		String token = apiAdapter.authGetToken();

		return token;
	}

	/**
	 * See https://www.last.fm/api/desktopauth
	 */
	@Override
	public String getUserAuthorizationPageUrl(String token) {
		// 3. Request authorization from the user
		String authPageUrl = apiAdapter.getUserAuthorizationPageUrl(token);
		return authPageUrl;
	}

	/**
	 * See https://www.last.fm/api/desktopauth
	 */
	@Override
	public boolean setSessionByToken(String token, String pageUrl) {
		boolean validUrl = apiAdapter.validatePageUrlForGetSession(token, pageUrl);
		if (!validUrl) {
			LOG.warn("Invalid authorization page URL: {}", pageUrl);
		}

		// 4. Fetch A Web Service Session
		Optional<LastFmSessionData> sessionData = apiAdapter.authGetSession(token);
		if (sessionData.isPresent()) {
			currentSessionData = sessionData.get();
			applicationConfigService.saveLastFmSessionData(currentSessionData);
			return true;
		}
		return false;
	}

	@Override
	public List<Track> getUserRecentTracks(boolean fetchMore) {
		if ((currentSessionData == null) || (currentSessionData.getUsername() == null)) {
			return new ArrayList<>();
		}

		if (fetchMore) {
			currentRecentTracksPageSize += RECENT_TRACKS_PAGE_STEP;
		}

		Optional<RecentTracks> recentTracks = apiAdapter.userGetRecentTracks(currentRecentTracksPageSize,
				currentSessionData.getUsername(), null, null, null, null);
		return recentTracks.map(RecentTracks::getTracks).orElse(new ArrayList<>());
	}

	@Override
	public TrackInfo getTrackInfo(String artistName, String trackTitle) {
		if ((currentSessionData == null) || (currentSessionData.getUsername() == null)) {
			return null;
		}

		return apiAdapter.getTrackInfo(null, trackTitle, artistName, currentSessionData.getUsername(), null)
				.orElse(null);
	}

	@Override
	public boolean updateNowPlaying(String artistName, String trackTitle, String albumName) {
		if ((currentSessionData == null) || (currentSessionData.getSessionKey() == null)) {
			return false;
		}

		Optional<UpdateNowPlayingResult> updateNowPlayingResult = apiAdapter.updateNowPlaying(
				currentSessionData.getSessionKey(), artistName, trackTitle, albumName, null);
		return updateNowPlayingResult.isPresent();
	}

	@Override
	public long calculateScrobbleDelay(PlaylistTrack track) {
		long trackLengthMs = track.getLength();

		// when playing from HTTP stream - length always 0, request length value from Last.fm
		if (MediaSourceType.HTTP_STREAM.equals(track.getSourceType())) {
			TrackInfo trackInfo = getTrackInfo(track.getArtist(), track.getTitle());
			if ((trackInfo == null) || (trackInfo.getDuration() == 0)) {
				// if on Last.fm also no information about track length - return default scrobble delay
				return SCROBBLE_DEFAULT_DELAY_MS;
			}
			trackLengthMs = trackInfo.getDuration();
		}

		trackLengthMs = trackLengthMs / 2;
		return (trackLengthMs < SCROBBLE_MAX_DELAY_MS) ?
				trackLengthMs : SCROBBLE_MAX_DELAY_MS;
	}

	@Override
	public boolean scrobbleTrack(String artistName, String trackTitle, String albumName, Date timestamp, Boolean chosenByUser) {
		if ((currentSessionData == null) || (currentSessionData.getSessionKey() == null)) {
			return false;
		}

		Optional<ScrobblesResult> scrobblesResult = apiAdapter.scrobbleTrack(
				currentSessionData.getSessionKey(), artistName, trackTitle, (int) (timestamp.getTime() / 1000),
				albumName, chosenByUser, null);
		return scrobblesResult.isPresent() && (scrobblesResult.get().getAccepted() == 1)
				&& !CollectionUtils.isEmpty(scrobblesResult.get().getTrackScrobbleResults());
	}

}
