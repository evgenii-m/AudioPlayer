package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/27/17
 */
public abstract class DefaultAudioPlayerEventAdapter implements AudioPlayerEventListener {

  @Override
  public void changedPlaylist(PlaylistData playlist) {
  }

  @Override
  public void createdNewPlaylist(PlaylistData newPlaylist) {
  }

  @Override
  public void changedTrackPosition(String playlistName, int trackPosition) {
  }

  @Override
  public void refreshTrackMediaInfo(int trackPosition, MediaInfoData mediaInfo) {
  }
}