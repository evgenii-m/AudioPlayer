package ru.push.caudioplayer.core.services;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/14/17
 */
public class ServicesConstants {
  public static final String CONF_ROOT_NODE = "configuration";

  public static final String LASTFM_NODE = "lastfm";
  public static final String LASTFM_USERNAME_NODE = LASTFM_NODE + ".username";
  public static final String LASTFM_PASSWORD_NODE = LASTFM_NODE + ".password";

  public static final String PLAYLISTS_SET_NODE = "playlists";
  public static final String ACTIVE_PLAYLIST_NAME_NODE = PLAYLISTS_SET_NODE + ".activeName";
  public static final String DISPLAYED_PLAYLIST_NAME_NODE = PLAYLISTS_SET_NODE + ".displayedName";
  public static final String PLAYLIST_NODE = PLAYLISTS_SET_NODE + ".playlist";
}
