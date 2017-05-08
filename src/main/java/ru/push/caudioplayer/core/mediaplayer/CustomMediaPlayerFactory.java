package ru.push.caudioplayer.core.mediaplayer;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public class CustomMediaPlayerFactory extends MediaPlayerFactory {

  public CustomMediaPlayerFactory() {
    this(new String[0]);
  }

  public CustomMediaPlayerFactory(String[] args) {
    super(args);
  }
}
