package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.pojo.LastFmTrackData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

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

  MediaInfoData getCurrentTrackInfo();

	void connectLastFm(Consumer<String> openAuthPageConsumer);

	String getDeezerUserAuthorizationPageUrl();

	/**
	 * Return recent tracks list from Last.fm service for current user in chronological order.
	 * If user account not connected then return empty list.
	 */
	List<LastFmTrackData> getRecentTracksFromLastFm();

	void stopApplication();

}
