package ru.push.caudioplayer.core.facades.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.services.AppConfigurationService;

import javax.annotation.PostConstruct;
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
  private List<PlaylistData> playlists;

  @Autowired
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
  private CustomPlaylistComponent playlistComponent;
  @Autowired
  private AppConfigurationService appConfigurationService;


  public DefaultAudioPlayerFacade() {
    eventListeners = new ArrayList<>();
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");
  }

  @Override
  public synchronized void addListener(AudioPlayerEventListener listener) {
    eventListeners.add(listener);
  }

  @Override
  public synchronized void removeListener(AudioPlayerEventListener listener) {
    eventListeners.remove(listener);
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    if (CollectionUtils.isEmpty(playlists)) {
      playlists = appConfigurationService.getPlaylists();
      playlists.stream()
          .filter(PlaylistData::isActive).findFirst()
          .ifPresent(activePlaylist -> playlistComponent.loadPlaylist(activePlaylist));
    }
    return playlists;
  }

  @Override
  public void createNewPlaylist() {
    PlaylistData newPlaylist = new PlaylistData(playlists.size());
    playlists.add(newPlaylist);
    eventListeners.forEach(listener -> listener.createdNewPlaylist(newPlaylist));
  }

  @Override
  public void playMedia(int trackPosition) {
    String trackPath = playlistComponent.getTrackPath(trackPosition);
    playerComponent.playMedia(Paths.get(trackPath).toUri().toString());
  }
}
