package ru.push.caudioplayer.core.mediaplayer.components.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 10.12.16
 */
public class DefaultCustomAudioPlayerComponent implements CustomAudioPlayerComponent {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultCustomAudioPlayerComponent.class);

  private static final int VOLUME_MAX_VALUE = 200;
  private static final int VOLUME_DEFAULT_VALUE = 70;

  private final CustomMediaPlayerFactory mediaPlayerFactory;
  private final MediaPlayer mediaPlayer;

  private int volume;

  public DefaultCustomAudioPlayerComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
    mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
    volume = VOLUME_DEFAULT_VALUE;
    mediaPlayer.addMediaPlayerEventListener(new DefaultMediaPlayerEventListener());
  }

  @Override
  public synchronized void addEventListener(MediaPlayerEventListener eventListener) {
    mediaPlayer.addMediaPlayerEventListener(eventListener);
  }

  @Override
  public synchronized void removeEventListener(MediaPlayerEventListener eventListener) {
    mediaPlayer.removeMediaPlayerEventListener(eventListener);
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
  public void setVolume(int newVolume) {
    volume = newVolume;
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.setVolume(newVolume);
    }
  }

  @Override
  public boolean playMedia(String resourceUri) {
    return mediaPlayer.playMedia(resourceUri);
  }

  @Override
  public void stop() {
    mediaPlayer.stop();
  }

  @Override
  public void pause() {
    mediaPlayer.pause();
  }

  @Override
  public void resume() {
    mediaPlayer.play();
  }

  @Override
  public float getPlaybackPosition() {
    return mediaPlayer.getPosition();
  }

  @Override
  public long getCurrentTrackLength() {
    return mediaPlayer.getLength();
  }

  @Override
  public void changePlaybackPosition(float newPosition) {
    if (!mediaPlayer.isSeekable()) {
      LOG.info("Media player not seekable");
      return;
    }

    if (newPosition < 0 || newPosition >= 1) {
      throw new IllegalArgumentException("Position for playback must be define in range between 0.0 and 1.0 " +
          "[newPosition = " + newPosition + "]");
    }
    mediaPlayer.setPosition(newPosition);
  }

  @Override
  public boolean isPlaying() {
		LOG.debug("isPlaying");
    return mediaPlayer.isPlaying();
  }

  @Override
  public void releaseComponent() {
    LOG.debug("releaseComponent");
    mediaPlayer.release();
  }


  private class DefaultMediaPlayerEventListener extends MediaPlayerEventAdapter {
    @Override
    public void playing(MediaPlayer mediaPlayer) {
      LOG.debug("playing");
      try {
        Thread.sleep(250);    // setting volume available only after playback + delay
        mediaPlayer.setVolume(volume);
      } catch (InterruptedException e) {
        LOG.error("InterruptedException whe set volume: " + e);
      }
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
