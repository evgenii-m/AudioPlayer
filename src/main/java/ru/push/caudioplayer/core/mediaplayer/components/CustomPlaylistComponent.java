package ru.push.caudioplayer.core.mediaplayer.components;

import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

import java.io.File;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomPlaylistComponent extends NativePlayerComponent {

  void loadPlaylists(List<PlaylistData> playlists, String activePlaylistName, String displayedPlaylistName);

  List<PlaylistData> getPlaylists();

  PlaylistData createNewPlaylist();

  boolean deletePlaylist(String playlistName);

  void renamePlaylist(String actualPlaylistName, String newPlaylistName);

  PlaylistData getActivePlaylist();

  PlaylistData getDisplayedPlaylist();

  boolean setDisplayedPlaylist(String playlistName);

  void setDisplayedPlaylist(PlaylistData playlist);

  PlaylistData getPlaylist(String playlistName);

  int getActiveTrackPosition();

  MediaInfoData playTrack(String playlistName, int trackPosition) throws IllegalArgumentException;

  MediaInfoData playCurrentTrack();

  MediaInfoData playNextTrack();

  MediaInfoData playPrevTrack();

  List<PlaylistData> addFilesToPlaylist(String playlistName, List<File> files);

  List<PlaylistData> deleteItemsFromPlaylist(String playlistName, List<Integer> itemsIndexes);

  List<PlaylistData> addLocationsToPlaylist(String playlistName, List<String> locations);
}
