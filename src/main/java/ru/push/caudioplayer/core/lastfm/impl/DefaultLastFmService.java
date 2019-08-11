package ru.push.caudioplayer.core.lastfm.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.lastfm.LastFmApiAdapter;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.LastFmUserData;
import ru.push.caudioplayer.core.services.AppConfigurationService;

import java.util.function.Consumer;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
@Component
public class DefaultLastFmService implements LastFmService {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultLastFmService.class);

  @Autowired
	private LastFmApiAdapter apiAdapter;
	@Autowired
	private AppConfigurationService appConfigurationService;

	/**
	 * See https://www.last.fm/api/desktopauth
	 */
	@Override
	public void connectLastFm(Consumer<String> openAuthPageConsumer) {
		// 1. API Key into adapter

		// 2. Fetch a request token
		String token = apiAdapter.authGetToken();

		// 3. Request authorization from the user
		String authPageUrl = apiAdapter.getUserAuthorizationPageUrl(token);
		openAuthPageConsumer.accept(authPageUrl);

		// 4. Fetch A Web Service Session
		Pair<String, String> usernameSessionKey = apiAdapter.authGetSession(token);
		appConfigurationService.saveLastFmUserData(usernameSessionKey.getKey(), usernameSessionKey.getValue());
	}

	@Override
  public void updateNowPlaying(String artistName, String trackTitle, LastFmUserData userData) {

  }

  @Override
  public void getNowPlaying() {

  }
}
