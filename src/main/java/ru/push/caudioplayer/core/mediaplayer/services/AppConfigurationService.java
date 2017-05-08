package ru.push.caudioplayer.core.mediaplayer.services;

import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public interface AppConfigurationService {

  List<PlaylistData> getPlaylists();

  void savePlaylists(List<PlaylistData> playlists);
}
