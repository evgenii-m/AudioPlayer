package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerEventListener {

  void changedPlaylist(PlaylistData playlist);

  void createdNewPlaylist(PlaylistData newPlaylist);

  void changedTrackPosition(PlaylistData playlist, int trackPosition);

  void refreshTrackMediaInfo(int trackPosition, MediaInfoData mediaInfo);

  void stopAudioPlayer();
}
