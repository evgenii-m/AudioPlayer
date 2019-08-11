package ru.push.caudioplayer.core.services;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/14/17
 */
public interface ConfigurationServiceConstants {
  String CONF_ROOT_NODE = "configuration";

  String PLAYLISTS_SET_NODE = "playlists";
  String ACTIVE_PLAYLIST_UID_NODE = PLAYLISTS_SET_NODE + ".activeUid";
  String DISPLAYED_PLAYLIST_UID_NODE = PLAYLISTS_SET_NODE + ".displayedUid";
  String PLAYLIST_NODE_NAME = "playlist";
  String PLAYLIST_NODE = PLAYLISTS_SET_NODE + "." + PLAYLIST_NODE_NAME;
  String PLAYLIST_NODE_ATTR_UID = "uid";
  String PLAYLIST_NODE_ATTR_NAME = "name";
  String PLAYLIST_NODE_ATTR_POSITION = "position";
  String PLAYLIST_TRACK_NODE_NAME = "track";
  String PLAYLIST_TRACK_NODE = PLAYLIST_NODE + "." + PLAYLIST_TRACK_NODE_NAME;
  String PLAYLIST_TRACK_NODE_ATTR_SOURCE_TYPE = "sourceType";

  String LASTFM_NODE = "lastfm";
  String LASTFM_USERNAME_NODE = LASTFM_NODE + ".username";
  String LASTFM_PASSWORD_NODE = LASTFM_NODE + ".password";

  String VIEW_NODE = "view";
  String PLAYLIST_CONTAINER_NODE = VIEW_NODE + ".playlistContainer";
  String PLAYLIST_CONTAINER_COLUMNS_SET_NODE_NAME = "columns";
  String PLAYLIST_CONTAINER_COLUMNS_SET_NODE = PLAYLIST_CONTAINER_NODE + "." + PLAYLIST_CONTAINER_COLUMNS_SET_NODE_NAME;
  String PLAYLIST_CONTAINER_COLUMN_NODE_NAME = "column";
  String PLAYLIST_CONTAINER_COLUMN_NODE = PLAYLIST_CONTAINER_COLUMNS_SET_NODE + "." + PLAYLIST_CONTAINER_COLUMN_NODE_NAME;
  String PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_NAME = "name";
  String PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_TITLE = "title";
  String PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_WIDTH = "width";
}