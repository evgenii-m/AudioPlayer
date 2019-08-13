package ru.push.caudioplayer.core.lastfm;

import java.util.function.Consumer;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
public interface LastFmService {

	void connectLastFm(Consumer<String> openAuthPageConsumer);

  void updateNowPlaying(String artistName, String trackName);

  void getNowPlaying();
}
