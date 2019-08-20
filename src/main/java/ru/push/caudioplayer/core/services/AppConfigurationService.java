package ru.push.caudioplayer.core.services;

import org.apache.commons.configuration2.ex.ConfigurationException;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.ui.configuration.PlaylistContainerViewConfigurations;

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

  void saveLastFmSessionData(LastFmSessionData sessionData);

	LastFmSessionData getLastFmSessionData();

	void saveDeezerAccessToken(String accessToken);

	String getDeezerAccessToken();


  // view configuration methods

  PlaylistContainerViewConfigurations getPlaylistContainerViewConfigurations() throws ConfigurationException;

  void savePlaylistContainerViewConfigurations(PlaylistContainerViewConfigurations viewConfigurations);
}
