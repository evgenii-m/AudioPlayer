package ru.push.caudioplayer.core.lastfm;

import ru.push.caudioplayer.core.lastfm.model.Track;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
public interface LastFmService {

	String getToken();

	String getUserAuthorizationPageUrl(String token);

	boolean setSessionByToken(String token, String pageUrl);

	List<Track> getUserRecentTracks();

  void updateNowPlaying(String artistName, String trackName);

  void getNowPlaying();
}
