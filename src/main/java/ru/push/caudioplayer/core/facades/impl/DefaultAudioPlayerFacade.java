package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public class DefaultAudioPlayerFacade implements AudioPlayerFacade {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAudioPlayerFacade.class);

  private final List<AudioPlayerEventListener> eventListeners;

  @Autowired
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
  private CustomPlaylistComponent playlistComponent;
  @Autowired
  private ApplicationConfigService applicationConfigService;
  @Autowired
  private MediaInfoDataLoaderService mediaInfoDataLoaderService;

  private AudioTrackData currentTrackInfoData;


  public DefaultAudioPlayerFacade() {
    currentTrackInfoData = new AudioTrackData();
    eventListeners = new ArrayList<>();
  }

  @PostConstruct
  public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

    playerComponent.addEventListener(new AudioPlayerFacadeEventListener());
  }

	@Override
  public synchronized void addEventListener(AudioPlayerEventListener listener) {
    eventListeners.add(listener);
  }

  @Override
  public synchronized void removeEventListener(AudioPlayerEventListener listener) {
    eventListeners.remove(listener);
  }

  @Override
  public void playTrack(String playlistUid, int trackPosition) {
    AudioTrackData trackInfo = playlistComponent.playTrack(playlistUid, trackPosition);
    playTrack(trackInfo);
  }

  @Override
  public void playCurrentTrack() {
    AudioTrackData trackInfo = playlistComponent.playCurrentTrack();
    playTrack(trackInfo);
  }

  @Override
  public void playNextTrack() {
    AudioTrackData trackInfo = playlistComponent.playNextTrack();
    playTrack(trackInfo);
  }

  @Override
  public void playPrevTrack() {
    AudioTrackData trackInfo = playlistComponent.playPrevTrack();
    playTrack(trackInfo);
  }

  private void playTrack(AudioTrackData trackInfo) {
    String resourceUri = MediaSourceType.FILE.equals(trackInfo.getSourceType()) ?
        Paths.get(trackInfo.getTrackPath()).toString() : trackInfo.getTrackPath();
    playerComponent.playMedia(resourceUri);
    currentTrackInfoData = trackInfo;
    eventListeners.forEach(listener ->
        listener.changedTrackPosition(playlistComponent.getActivePlaylist(),
            playlistComponent.getActiveTrackPosition())
    );
  }

  @Override
  public AudioTrackData getCurrentTrackInfo() {
    return currentTrackInfoData;
  }

	@Override
  public void stopApplication() {
    // TODO: think about remove this saving
    applicationConfigService.saveAllPlaylists(
        playlistComponent.getPlaylists(),
        playlistComponent.getActivePlaylist(),
        playlistComponent.getDisplayedPlaylist()
    );
    playerComponent.releaseComponent();
    playlistComponent.releaseComponent();
  }


  private class AudioPlayerFacadeEventListener extends MediaPlayerEventAdapter {

    @Override
    public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
      if (mediaPlayer.isPlaying()) {  // media changes actual only when playing media
        LOG.debug("mediaMetaChanged");
        MediaMeta mediaMeta = mediaPlayer.getMediaMeta();
        if (mediaMeta != null) {
          MediaSourceType sourceType = (currentTrackInfoData.getSourceType() != null) ?
              currentTrackInfoData.getSourceType() : MediaSourceType.FILE;
          mediaInfoDataLoaderService.fillMediaInfoFromMediaMeta(currentTrackInfoData, mediaMeta, sourceType);
          mediaMeta.release();
        } else {
          LOG.error("Media info is null!");
        }

        int currentTrackPosition = playlistComponent.getActiveTrackPosition();
        eventListeners.forEach(listener ->
            listener.refreshTrackMediaInfo(currentTrackPosition, currentTrackInfoData));
      }
    }
  }
}
