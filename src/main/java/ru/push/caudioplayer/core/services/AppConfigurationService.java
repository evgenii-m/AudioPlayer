package ru.push.caudioplayer.core.services;

import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public interface AppConfigurationService {

  String getActivePlaylistName();

  String getDisplayedPlaylistName();

  List<PlaylistData> getPlaylists();

  void saveActivePlaylist(PlaylistData activePlaylist) throws IllegalArgumentException;

  void saveDisplayedPlaylist(PlaylistData displayedPlaylist) throws IllegalArgumentException;

  void savePlaylist(PlaylistData playlistData) throws IllegalArgumentException;

  void renamePlaylist(PlaylistData playlistData) throws IllegalArgumentException;

  void deletePlaylist(PlaylistData playlistData) throws IllegalArgumentException;

  void saveAllPlaylists(List<PlaylistData> playlistsData, PlaylistData activePlaylist, PlaylistData displayedPlaylist)
      throws IllegalArgumentException;

  void saveLastFmUserData(@NotNull String username, @NotNull String password)  throws IllegalArgumentException;
}
