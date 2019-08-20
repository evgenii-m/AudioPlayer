package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.PlaylistData;

import java.io.File;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerFacade {

  void addEventListener(AudioPlayerEventListener listener);

  void removeEventListener(AudioPlayerEventListener listener);

  void refreshPlaylists();

  List<PlaylistData> getPlaylists();

  PlaylistData getActivePlaylist();

  PlaylistData getDisplayedPlaylist();

  PlaylistData getPlaylist(String playlistUid);

  PlaylistData showPlaylist(String playlistUid);

  PlaylistData showActivePlaylist();

  PlaylistData createNewPlaylist();

  boolean deletePlaylist(String playlistUid);

  void renamePlaylist(String playlistUid, String newPlaylistName);

  void addFilesToPlaylist(List<File> files);

  void deleteItemsFromPlaylist(List<Integer> itemsIndexes);

  void addLocationsToPlaylist(List<String> locations);

  void playTrack(String playlistUid, int trackPosition);

  void playCurrentTrack();

  void playNextTrack();

  void playPrevTrack();

  AudioTrackData getCurrentTrackInfo();

	void stopApplication();

}
