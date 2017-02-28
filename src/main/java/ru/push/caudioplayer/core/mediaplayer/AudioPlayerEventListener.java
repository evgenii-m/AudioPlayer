package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerEventListener {

  void changedPlaylist(PlaylistData playlist);

  void createdNewPlaylist(PlaylistData newPlaylist);

  void changedTrackPosition(String playlistName, int trackPosition);

  void stopAudioPlayer();
}
