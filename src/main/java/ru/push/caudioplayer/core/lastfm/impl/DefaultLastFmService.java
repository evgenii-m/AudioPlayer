package ru.push.caudioplayer.core.lastfm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.lastfm.LastFmApiAdapter;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.lastfm.domain.RecentTracks;
import ru.push.caudioplayer.core.lastfm.domain.Track;
import ru.push.caudioplayer.core.config.ApplicationConfigService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
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
	@Override
	public void connectLastFm(Consumer<String> openAuthPageConsumer) {

		LastFmSessionData sessionData = applicationConfigService.getLastFmSessionData();
		if (sessionData != null) {
			LOG.warn("Last.fm user data already set in configuration, they will be overwritten: username = {}, session key = {}",
					sessionData.getUsername(), sessionData.getSessionKey());
		}

		// 1. API Key into adapter

		// 2. Fetch a request token
		String token = apiAdapter.authGetToken();

		// 3. Request authorization from the user
		String authPageUrl = apiAdapter.getUserAuthorizationPageUrl(token);
		openAuthPageConsumer.accept(authPageUrl);

		// 4. Fetch A Web Service Session
		currentSessionData = apiAdapter.authGetSession(token);
		applicationConfigService.saveLastFmSessionData(currentSessionData);
	}

	@Override
	public List<Track> getUserRecentTracks() {
		if ((currentSessionData == null) || (currentSessionData.getUsername() == null)) {
			return new ArrayList<>();
		}

		RecentTracks recentTracks = apiAdapter.userGetRecentTracks(RECENT_TRACKS_COUNT, currentSessionData.getUsername(),
				null, null, null, null);
		return recentTracks.getTracks();
	}

	@Override
  public void updateNowPlaying(String artistName, String trackTitle) {

  }

  @Override
  public void getNowPlaying() {

  }
}
