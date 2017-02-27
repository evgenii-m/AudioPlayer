package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/27/17
 */
public abstract class DefaultAudioPlayerEventAdapter implements AudioPlayerEventListener {

  @Override
  public void createdNewPlaylist(PlaylistData newPlaylist) {
  }

  @Override
  public void changedTrackPosition(String playlistName, int trackPosition) {
  }
}
