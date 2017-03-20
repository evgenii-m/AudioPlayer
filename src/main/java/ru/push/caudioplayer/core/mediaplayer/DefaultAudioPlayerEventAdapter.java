package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.model.MediaInfoModel;
import ru.push.caudioplayer.core.mediaplayer.model.PlaylistModel;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/27/17
 */
public abstract class DefaultAudioPlayerEventAdapter implements AudioPlayerEventListener {

  @Override
  public void changedPlaylist(PlaylistModel playlist) {
  }

  @Override
  public void createdNewPlaylist(PlaylistModel newPlaylist) {
  }

  @Override
  public void changedTrackPosition(String playlistName, int trackPosition) {
  }

  @Override
  public void refreshTrackMediaInfo(int trackPosition, MediaInfoModel mediaInfo) {
  }
}
