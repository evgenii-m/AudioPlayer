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

  void saveActivePlaylist(PlaylistData activePlaylist);

  void saveDisplayedPlaylist(PlaylistData displayedPlaylist);

  void savePlaylist(PlaylistData playlistData);

  void renamePlaylist(PlaylistData playlistData, String newPlaylistName);

  void deletePlaylist(PlaylistData playlistData);

  void saveAllPlaylists(List<PlaylistData> playlistsData, PlaylistData activePlaylist, PlaylistData displayedPlaylist);

  void saveLastFmUserData(String username, String password);
}
