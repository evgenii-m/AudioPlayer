package ru.push.caudioplayer.core.mediaplayer.components;

import ru.push.caudioplayer.core.mediaplayer.domain.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.domain.PlaylistData;

import java.io.File;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomPlaylistComponent extends NativePlayerComponent {

  boolean loadPlaylists(List<PlaylistData> playlists, String activePlaylistUid, String displayedPlaylistUid);

  List<PlaylistData> getPlaylists();

  PlaylistData createNewPlaylist();

  boolean deletePlaylist(String playlistUid);

  PlaylistData renamePlaylist(String playlistUid, String newPlaylistName);

  PlaylistData getActivePlaylist();

  PlaylistData getDisplayedPlaylist();

  boolean setDisplayedPlaylist(String playlistUid);

  void setDisplayedPlaylist(PlaylistData playlist);

  PlaylistData getPlaylist(String playlistUid);

  int getActiveTrackPosition();

  MediaInfoData playTrack(String playlistUid, int trackPosition) throws IllegalArgumentException;

  MediaInfoData playCurrentTrack();

  MediaInfoData playNextTrack();

  MediaInfoData playPrevTrack();

  PlaylistData addFilesToPlaylist(String playlistUid, List<File> files);

  PlaylistData deleteItemsFromPlaylist(String playlistUid, List<Integer> itemsIndexes);

  PlaylistData addLocationsToPlaylist(String playlistUid, List<String> locations);
}
