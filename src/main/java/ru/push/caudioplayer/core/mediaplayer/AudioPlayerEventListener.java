package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.model.MediaInfoModel;
import ru.push.caudioplayer.core.mediaplayer.model.PlaylistModel;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerEventListener {

  void changedPlaylist(PlaylistModel playlist);

  void createdNewPlaylist(PlaylistModel newPlaylist);

  void changedTrackPosition(String playlistName, int trackPosition);

  void refreshTrackMediaInfo(int trackPosition, MediaInfoModel mediaInfo);

  void stopAudioPlayer();
}
