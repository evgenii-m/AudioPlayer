package ru.push.caudioplayer.core.mediaplayer.components;

import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;

import java.io.File;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomPlaylistComponent extends NativePlayerComponent {

	/**
	 * @return if true - all right, if false - with errors and need refresh config file
	 */
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

  AudioTrackData playTrack(String playlistUid, int trackPosition) throws IllegalArgumentException;

  AudioTrackData playCurrentTrack();

  AudioTrackData playNextTrack();

  AudioTrackData playPrevTrack();

  PlaylistData addFilesToPlaylist(String playlistUid, List<File> files);

  PlaylistData deleteItemsFromPlaylist(String playlistUid, List<Integer> itemsIndexes);

  PlaylistData addLocationsToPlaylist(String playlistUid, List<String> locations);
}
