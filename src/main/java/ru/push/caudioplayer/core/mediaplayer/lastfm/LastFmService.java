package ru.push.caudioplayer.core.mediaplayer.lastfm;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
public interface LastFmService {

  void updateNowPlaying(String artistName, String trackName, LastFmUserData userData);
}
