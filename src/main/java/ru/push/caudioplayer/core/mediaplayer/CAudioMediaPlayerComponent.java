package ru.push.caudioplayer.core.mediaplayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 10.12.16
 */
public class CAudioMediaPlayerComponent extends AudioMediaPlayerComponent {
  private static final Logger LOGGER = LoggerFactory.getLogger(CAudioMediaPlayerComponent.class);

  public CAudioMediaPlayerComponent()
  {
    super();

    this.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      @Override
      public void playing(MediaPlayer mediaPlayer) {
        LOGGER.debug("playing");
      }

      @Override
      public void paused(MediaPlayer mediaPlayer) {
        LOGGER.debug("paused");
      }

      @Override
      public void stopped(MediaPlayer mediaPlayer) {
        LOGGER.debug("stopped");
      }

      @Override
      public void finished(MediaPlayer mediaPlayer) {
        LOGGER.debug("finished");
      }

      @Override
      public void error(MediaPlayer mediaPlayer) {
        LOGGER.debug("error");
      }
    });
  }

}
