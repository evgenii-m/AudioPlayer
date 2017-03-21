package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

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
