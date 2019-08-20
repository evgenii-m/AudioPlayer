package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerEventListener {

  void changedPlaylist(PlaylistData playlist);

  void createdNewPlaylist(PlaylistData newPlaylist);

  void changedTrackPosition(PlaylistData playlist, int trackPosition);

  void refreshTrackMediaInfo(int trackPosition, AudioTrackData mediaInfo);

  void renamedPlaylist(PlaylistData playlistData);
}
