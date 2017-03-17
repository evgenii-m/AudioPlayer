package ru.push.caudioplayer.core.mediaplayer.components;

import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomAudioPlayerComponent extends NativePlayerComponent {

  void addEventListener(MediaPlayerEventListener eventListener);

  int getMaxVolume();

  int getVolume();

  void setVolume(int newVolume);

  boolean playMedia(String resourceUri);

  void stop();

  void pause();

  void resume();

  float getPlaybackPosition();

  long getCurrentTrackLength();

  void changePlaybackPosition(float newPosition);

  boolean isPlaying();

}
