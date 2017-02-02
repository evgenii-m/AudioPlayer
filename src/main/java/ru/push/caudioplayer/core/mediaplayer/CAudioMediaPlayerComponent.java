package ru.push.caudioplayer.core.mediaplayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import java.util.concurrent.TimeUnit;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 10.12.16
 */
public class CAudioMediaPlayerComponent extends AudioMediaPlayerComponent {
  private static final Logger LOG = LoggerFactory.getLogger(CAudioMediaPlayerComponent.class);

  public static final int VOLUME_MAX_VALUE = 200;
  public static final int VOLUME_DEFAULT_VALUE = 100;

  private int volume;

  public CAudioMediaPlayerComponent()
  {
    super();
    volume = VOLUME_DEFAULT_VALUE;
    this.getMediaPlayer().addMediaPlayerEventListener(new CAudioMediaPlayerEventListener());
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  private class CAudioMediaPlayerEventListener extends MediaPlayerEventAdapter {
    @Override
    public void playing(MediaPlayer mediaPlayer) {
      LOG.debug("playing");
      try {
        Thread.sleep(100);    // setting volume available oly after playback + delay
        mediaPlayer.setVolume(volume);
      } catch (InterruptedException e) {
        LOG.error("InterruptedException whe set volume: " + e);
      }
      LOG.debug("Volume: " + mediaPlayer.getVolume());
    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {
      LOG.debug("paused");
    }

    @Override
    public void stopped(MediaPlayer mediaPlayer) {
      LOG.debug("stopped");
    }

    @Override
    public void finished(MediaPlayer mediaPlayer) {
      LOG.debug("finished");
    }

    @Override
    public void error(MediaPlayer mediaPlayer) {
      LOG.debug("error");
    }
  }
}
