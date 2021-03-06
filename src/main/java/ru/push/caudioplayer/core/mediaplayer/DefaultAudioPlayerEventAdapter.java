package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.facades.dto.NotificationData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.TrackData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/27/17
 */
public abstract class DefaultAudioPlayerEventAdapter implements AudioPlayerEventListener {

  @Override
  public void changedPlaylist(PlaylistData playlistData) {
  }

  @Override
  public void createdNewPlaylist(PlaylistData playlistData) {
  }

  @Override
  public void changedTrackData(PlaylistData playlistData, TrackData trackData) {
  }

  @Override
  public void renamedPlaylist(PlaylistData playlistData) {
  }

	@Override
	public void deletedPlaylist(PlaylistData playlistData) {
	}

	@Override
	public void changedNowPlayingTrack(TrackData trackData) {
	}

	@Override
	public void obtainedNotification(NotificationData notificationData) {

	}
}
