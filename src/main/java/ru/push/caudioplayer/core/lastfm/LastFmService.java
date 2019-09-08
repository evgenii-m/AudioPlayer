package ru.push.caudioplayer.core.lastfm;

import ru.push.caudioplayer.core.lastfm.model.Track;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
public interface LastFmService {

	void connectLastFm(Consumer<String> openAuthPageConsumer);

	List<Track> getUserRecentTracks();

  void updateNowPlaying(String artistName, String trackName);

  void getNowPlaying();
}
