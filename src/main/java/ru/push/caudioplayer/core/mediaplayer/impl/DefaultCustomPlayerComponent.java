package ru.push.caudioplayer.core.mediaplayer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomPlayerComponent;
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 10.12.16
 */
public class DefaultCustomPlayerComponent extends AudioMediaPlayerComponent implements CustomPlayerComponent {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultCustomPlayerComponent.class);

  private static final int VOLUME_MAX_VALUE = 200;
  private static final int VOLUME_DEFAULT_VALUE = 100;

  private int volume;

  public DefaultCustomPlayerComponent() {
    super();
    volume = VOLUME_DEFAULT_VALUE;
    this.getMediaPlayer().addMediaPlayerEventListener(new CustomMediaPlayerEventListener());
  }

  @Override
  public int getMaxVolume() {
    return VOLUME_MAX_VALUE;
  }

  @Override
  public int getVolume() {
    return volume;
  }

  @Override
  public void setVolume(int volume) {
    this.volume = volume;
    this.getMediaPlayer().setVolume(volume);
  }

  @Override
  public boolean playMedia(String resourceUri) {
    return this.getMediaPlayer().playMedia(resourceUri);
  }

  @Override
  public void stop() {
    this.getMediaPlayer().stop();
  }

  @Override
  public void pause() {
    this.getMediaPlayer().pause();
  }


  private class CustomMediaPlayerEventListener extends MediaPlayerEventAdapter {
    @Override
    public void playing(MediaPlayer mediaPlayer) {
      LOG.debug("playing");
      try {
        Thread.sleep(250);    // setting volume available only after playback + delay
        mediaPlayer.setVolume(volume);
      } catch (InterruptedException e) {
        LOG.error("InterruptedException whe set volume: " + e);
      }
      LOG.debug("Volume: " + mediaPlayer.getVolume());
    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {
      LOG.debug("paused");
      LOG.debug("Volume: " + mediaPlayer.getVolume());
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
