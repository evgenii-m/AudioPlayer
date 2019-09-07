package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.TrackData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerEventListener {

  void changedPlaylist(PlaylistData playlistData);

  void createdNewPlaylist(PlaylistData playlistData);

  void changedTrackPosition(PlaylistData playlistData, int trackIndex);

  void changedPlaylistTrackData(PlaylistData playlistData, TrackData trackData, int trackIndex);

  void renamedPlaylist(PlaylistData playlistData);

  void deletedPlaylist(PlaylistData playlistData);
}
