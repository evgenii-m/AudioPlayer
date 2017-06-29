package ru.push.caudioplayer.core.services;

import org.apache.commons.configuration2.ex.ConfigurationException;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.ui.PlaylistContainerViewConfigurations;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public interface AppConfigurationService {

  // playlists configuration methods

  String getActivePlaylistUid();

  String getDisplayedPlaylistUid();

  List<PlaylistData> getPlaylists();

  void saveActivePlaylist(PlaylistData activePlaylist);

  void saveDisplayedPlaylist(PlaylistData displayedPlaylist);

  void savePlaylist(PlaylistData playlistData);

  void renamePlaylist(PlaylistData playlistData);

  void deletePlaylist(PlaylistData playlistData);

  void saveAllPlaylists(List<PlaylistData> playlistsData, PlaylistData activePlaylist, PlaylistData displayedPlaylist);


  // user configuration methods

  void saveLastFmUserData(String username, String password);

  // TODO: add getLastFmUserData method


  // view configuration methods

  PlaylistContainerViewConfigurations getPlaylistContainerViewConfigurations() throws ConfigurationException;

  // TODO: add savePlaylistContainerViewConfigurations method
}
