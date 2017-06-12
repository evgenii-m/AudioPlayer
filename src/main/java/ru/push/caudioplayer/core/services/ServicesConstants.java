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
  public static final String PLAYLIST_NODE_NAME = "playlist";
  public static final String PLAYLIST_NODE = PLAYLISTS_SET_NODE + "." + PLAYLIST_NODE_NAME;
  public static final String PLAYLIST_NODE_ATTR_UID = "uid";
  public static final String PLAYLIST_NODE_ATTR_NAME = "name";
  public static final String PLAYLIST_NODE_ATTR_POSITION = "position";
  public static final String PLAYLIST_TRACK_NODE_NAME = "track";
  public static final String PLAYLIST_TRACK_NODE = PLAYLIST_NODE + "." + PLAYLIST_TRACK_NODE_NAME;
  public static final String PLAYLIST_TRACK_NODE_ATTR_SOURCE_TYPE = "sourceType";
}
