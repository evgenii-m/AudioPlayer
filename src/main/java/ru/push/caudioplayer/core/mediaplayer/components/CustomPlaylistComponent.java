package ru.push.caudioplayer.core.mediaplayer.components;

import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomPlaylistComponent extends NativePlayerComponent {

  PlaylistData loadPlaylist(PlaylistData playlistData);

  PlaylistData getPlaylist();
}
