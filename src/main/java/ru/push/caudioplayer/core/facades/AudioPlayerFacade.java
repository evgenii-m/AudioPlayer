package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;

import java.io.File;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerFacade {

  void addListener(AudioPlayerEventListener listener);

  void removeListener(AudioPlayerEventListener listener);

  List<PlaylistData> getPlaylists();

  PlaylistData getActivePlaylist();

  PlaylistData getPlaylist(String playlistName);

  PlaylistData showPlaylist(String playlistName);

  void createNewPlaylist();

  boolean deletePlaylist(String playlistName);

  void renamePlaylist(String actualPlaylistName, String newPlaylistName);

  void addFilesToPlaylist(List<File> files);

  void deleteItemsFromPlaylist(List<Integer> itemsIndexes);

  void addLocationsToPlaylist(List<String> locations);

  void playTrack(String playlistName, int trackPosition);

  void playCurrentTrack();

  void playNextTrack();

  void playPrevTrack();

  void stopApplication();

}
