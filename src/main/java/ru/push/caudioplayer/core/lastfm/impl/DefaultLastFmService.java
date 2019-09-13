package ru.push.caudioplayer.core.lastfm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.lastfm.LastFmApiAdapter;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.lastfm.model.RecentTracks;
import ru.push.caudioplayer.core.lastfm.model.Track;
import ru.push.caudioplayer.core.config.ApplicationConfigService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
@Component
public class DefaultLastFmService implements LastFmService {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultLastFmService.class);

  private static final int RECENT_TRACKS_COUNT = 15;

  @Autowired
	private LastFmApiAdapter apiAdapter;
	@Autowired
	private ApplicationConfigService applicationConfigService;

	private LastFmSessionData currentSessionData;


	@PostConstruct
	public void init() {
		LastFmSessionData sessionData = applicationConfigService.getLastFmSessionData();
		if (sessionData != null) {
			LOG.info("Load Last.fm session from configuration: username = {}, session key = {}",
					sessionData.getUsername(), sessionData.getSessionKey());
			currentSessionData = sessionData;
		}
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
	public List<Track> getUserRecentTracks() {
		if ((currentSessionData == null) || (currentSessionData.getUsername() == null)) {
			return new ArrayList<>();
		}

		Optional<RecentTracks> recentTracks = apiAdapter.userGetRecentTracks(RECENT_TRACKS_COUNT,
				currentSessionData.getUsername(), null, null, null, null);
		return recentTracks.map(RecentTracks::getTracks).orElse(new ArrayList<>());
	}

	@Override
  public void updateNowPlaying(String artistName, String trackTitle) {

  }

  @Override
  public void getNowPlaying() {

  }
}
