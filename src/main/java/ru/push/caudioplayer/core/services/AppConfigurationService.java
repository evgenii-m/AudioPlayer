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

  void saveActivePlaylist(@NotNull String activePlaylistName);

  void saveDisplayedPlaylist(@NotNull String displayedPlaylistName);

  void savePlaylists(@NotNull List<PlaylistData> playlistsData);

  void savePlaylists(@NotNull List<PlaylistData> playlistsData, @NotNull String activePlaylistName,
                     @NotNull String displayedPlaylistName);
}
