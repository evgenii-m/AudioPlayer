package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.dto.TrackInfoData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomAudioPlayerEventListener {

  void playbackStarts(TrackInfoData trackInfo);

  /**
   * Playback position changed.
   *
   * @param newPosition   new playback position defined in range between 0.0 and 1.0
   */
  void positionChanged(float newPosition);
}
