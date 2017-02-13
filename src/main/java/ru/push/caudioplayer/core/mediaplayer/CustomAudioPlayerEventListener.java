package ru.push.caudioplayer.core.mediaplayer;

import ru.push.caudioplayer.core.mediaplayer.dto.TrackInfoData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomAudioPlayerEventListener {

  void playbackStarts(TrackInfoData trackInfo);

  void positionChanged(float newPosition);
}
