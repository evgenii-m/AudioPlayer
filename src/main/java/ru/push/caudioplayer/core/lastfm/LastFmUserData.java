package ru.push.caudioplayer.core.lastfm;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
public class LastFmUserData {
  private final String username;
  private final String password;

  public LastFmUserData(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
