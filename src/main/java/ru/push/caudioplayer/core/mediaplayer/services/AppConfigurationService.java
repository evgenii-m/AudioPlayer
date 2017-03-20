package ru.push.caudioplayer.core.mediaplayer.services;

import ru.push.caudioplayer.core.mediaplayer.model.PlaylistModel;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public interface AppConfigurationService {

  List<PlaylistModel> getPlaylists();

  void savePlaylists(List<PlaylistModel> playlists);
}
