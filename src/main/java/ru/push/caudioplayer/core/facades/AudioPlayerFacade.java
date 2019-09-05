package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerFacade {

  void addEventListener(AudioPlayerEventListener listener);

  void removeEventListener(AudioPlayerEventListener listener);

  void playTrack(String playlistUid, int trackIndex);

  void playCurrentTrack();

  void playNextTrack();

  void playPrevTrack();

  PlaylistItem getActivePlaylistTrack();

	void stopApplication();

}
