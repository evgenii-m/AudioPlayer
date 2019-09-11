package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;

import java.util.Optional;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerFacade {

  void addEventListener(AudioPlayerEventListener listener);

  void removeEventListener(AudioPlayerEventListener listener);

	void resumePlayingTrack();

  void playTrack(String playlistUid, String trackUid);

  void playNextTrack();

  void playPrevTrack();

	Optional<TrackData> getActivePlaylistTrack();

	void pauseCurrentTrack();

	void stopPlaying();

	void stopApplication();

}
