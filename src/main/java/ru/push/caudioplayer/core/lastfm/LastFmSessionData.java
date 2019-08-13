package ru.push.caudioplayer.core.lastfm;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
public class LastFmSessionData {
  private final String username;
  private final String sessionKey;

  public LastFmSessionData(String username, String sessionKey) {
    this.username = username;
    this.sessionKey = sessionKey;
  }

  public String getUsername() {
    return username;
  }

  public String getSessionKey() {
    return sessionKey;
  }
}
