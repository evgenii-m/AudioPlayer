package ru.push.caudioplayer.core.mediaplayer.components;

import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;

import java.io.File;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomPlaylistComponent extends NativePlayerComponent {

  void loadPlaylists(List<PlaylistData> playlists);

  List<PlaylistData> getPlaylists();

  PlaylistData createNewPlaylist();

  PlaylistData getActivePlaylist();

  PlaylistData getPlaylist(String playlistName);

  int getActiveTrackPosition();

  String playTrack(String playlistName, int trackPosition);

  String playCurrentTrack();

  String playNextTrack();

  String playPrevTrack();

  List<PlaylistData> addFilesToPlaylist(String playlistName, List<File> files);

  List<PlaylistData> deleteItemsFromPlaylist(String playlistName, List<Integer> itemsIndexes);
}
