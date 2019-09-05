package ru.push.caudioplayer.core.config;

import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.config.dto.PlaylistContainerViewConfigurations;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public interface ApplicationConfigService {

  // playlists configuration methods

  String getActivePlaylistUid();

  String getDisplayedPlaylistUid();

  List<String> getLocalPlaylistsUid();

  void saveActivePlaylist(String playlistUid);

  void saveDisplayedPlaylist(String playlistUid);

  void appendPlaylist(String playlistUid);

  void removePlaylist(String playlistUid);


  // user configuration methods

  void saveLastFmSessionData(LastFmSessionData sessionData);

	LastFmSessionData getLastFmSessionData();

	void saveDeezerAccessToken(String accessToken);

	String getDeezerAccessToken();


  // view configuration methods

  PlaylistContainerViewConfigurations getPlaylistContainerViewConfigurations();

	PlaylistContainerViewConfigurations getDeezerPlaylistContainerViewConfigurations();

  void savePlaylistContainerViewConfigurations(PlaylistContainerViewConfigurations viewConfigurations);
}
