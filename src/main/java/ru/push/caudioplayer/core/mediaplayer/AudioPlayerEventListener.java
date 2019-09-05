package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerEventListener {

  void changedPlaylist(Playlist playlist);

  void createdNewPlaylist(Playlist newPlaylist);

  void changedTrackPosition(Playlist playlist, int trackPosition);

  void changedPlaylistItemTrack(int trackPosition, PlaylistItem playlistItem);

  void renamedPlaylist(Playlist playlist);
}
