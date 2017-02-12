package ru.push.caudioplayer.core.mediaplayer;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public interface CustomPlayerComponent {
  int getMaxVolume();

  int getVolume();

  void setVolume(int newVolume);

  boolean playMedia(String resourceUri);

  void stop();

  void pause();
}
