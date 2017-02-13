package ru.push.caudioplayer.core.mediaplayer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.CustomAudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.dto.TrackInfoData;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 10.12.16
 */
public class DefaultCustomAudioPlayerComponent implements CustomAudioPlayerComponent {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultCustomAudioPlayerComponent.class);

  private static final int VOLUME_MAX_VALUE = 200;
  private static final int VOLUME_DEFAULT_VALUE = 10;

  private final List<CustomAudioPlayerEventListener> audioPlayerEventListeners;
  private final CustomMediaPlayerFactory mediaPlayerFactory;
  private final MediaPlayer mediaPlayer;

  private int volume;


  public DefaultCustomAudioPlayerComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
    this.mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
    this.volume = VOLUME_DEFAULT_VALUE;
    this.mediaPlayer.addMediaPlayerEventListener(new DefaultMediaPlayerEventListener());
    audioPlayerEventListeners = new ArrayList<>();
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
    mediaPlayer.setVolume(newVolume);
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
  public TrackInfoData getCurrentTrackInfo() {
    TrackInfoData trackInfoData = new TrackInfoData();
    trackInfoData.setDuration(mediaPlayer.getLength());
    return trackInfoData;
  }

  @Override
  public void addEventListener(CustomAudioPlayerEventListener eventListener) {
    audioPlayerEventListeners.add(eventListener);
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
        audioPlayerEventListeners.forEach(eventListener -> eventListener.playbackStarts(getCurrentTrackInfo()));
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

    @Override
    public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
      // TODO: think about reducing event period or uses another event for refresh track position indication,
      // because this event triggered with long period (~270 ms), that leads to uneven progress indication in UI
      audioPlayerEventListeners.forEach(eventListener -> eventListener.positionChanged(newPosition));
    }
  }
}
