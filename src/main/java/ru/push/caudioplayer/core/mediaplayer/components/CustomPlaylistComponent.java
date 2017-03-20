package ru.push.caudioplayer.core.mediaplayer.components;

import ru.push.caudioplayer.core.mediaplayer.model.MediaInfoModel;
import ru.push.caudioplayer.core.mediaplayer.model.PlaylistModel;

import java.io.File;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomPlaylistComponent extends NativePlayerComponent {

  void loadPlaylists(List<PlaylistModel> playlists);

  List<PlaylistModel> getPlaylists();

  PlaylistModel createNewPlaylist();

  boolean deletePlaylist(String playlistName);

  void renamePlaylist(String actualPlaylistName, String newPlaylistName);

  PlaylistModel getActivePlaylist();

  PlaylistModel getPlaylist(String playlistName);

  int getActiveTrackPosition();

  MediaInfoModel playTrack(String playlistName, int trackPosition);

  MediaInfoModel playCurrentTrack();

  MediaInfoModel playNextTrack();

  MediaInfoModel playPrevTrack();

  List<PlaylistModel> addFilesToPlaylist(String playlistName, List<File> files);

  List<PlaylistModel> deleteItemsFromPlaylist(String playlistName, List<Integer> itemsIndexes);

  List<PlaylistModel> addLocationsToPlaylist(String playlistName, List<String> locations);
}
