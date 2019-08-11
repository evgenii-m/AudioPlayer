package ru.push.caudioplayer.core.lastfm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.lastfm.LastFmApiAdapter;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.LastFmUserData;

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

	@Override
	public void connectLastFm(Consumer<String> openAuthPageConsumer) {
		String token = apiAdapter.authGetToken();
		String authPageUrl = apiAdapter.getUserAuthorizationPageUrl(token);
		openAuthPageConsumer.accept(authPageUrl);
	}

	@Override
  public void updateNowPlaying(String artistName, String trackTitle, LastFmUserData userData) {

  }

  @Override
  public void getNowPlaying() {

  }
}
