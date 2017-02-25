package ru.push.caudioplayer.core.mediaplayer.components;

import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomAudioPlayerComponent extends NativePlayerComponent {

  int getMaxVolume();

  int getVolume();

  void setVolume(int newVolume);

  boolean playMedia(String resourceUri);

  void stop();

  void pause();

  void resume();

  float getPlaybackPosition();

  void changePlaybackPosition(float newPosition);

  boolean isPlaying();

  MediaInfoData getCurrentTrackInfo();

}
