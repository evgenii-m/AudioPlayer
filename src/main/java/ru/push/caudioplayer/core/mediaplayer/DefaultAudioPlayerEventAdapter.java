package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/27/17
 */
public abstract class DefaultAudioPlayerEventAdapter implements AudioPlayerEventListener {

  @Override
  public void changedPlaylist(Playlist playlist) {
  }

  @Override
  public void createdNewPlaylist(Playlist newPlaylist) {
  }

  @Override
  public void changedTrackPosition(Playlist playlist, int trackPosition) {
  }

  @Override
  public void changedPlaylistItemTrack(int trackPosition, PlaylistItem mediaInfo) {
  }

  @Override
  public void renamedPlaylist(Playlist playlist) {
  }
}
