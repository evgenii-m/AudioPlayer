package ru.push.caudioplayer.core.facades.impl;

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
  private AppConfigurationService appConfigurationService;
  private PlaylistData displayedPlaylist;


  public DefaultAudioPlayerFacade() {
    eventListeners = new ArrayList<>();
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");
    playlistComponent.loadPlaylists(appConfigurationService.getPlaylists());
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
    return playlistComponent.getPlaylists();
  }

  @Override
  public PlaylistData getActivePlaylist() {
    return playlistComponent.getActivePlaylist();
  }

  @Override
  public PlaylistData getPlaylist(String playlistName) {
    return playlistComponent.getPlaylist(playlistName);
  }

  @Override
  public PlaylistData showPlaylist(String playlistName) {
    displayedPlaylist = getPlaylist(playlistName);
    return displayedPlaylist;
  }

  @Override
  public void createNewPlaylist() {
    PlaylistData newPlaylist = playlistComponent.createNewPlaylist();
    eventListeners.forEach(listener -> listener.createdNewPlaylist(newPlaylist));
    appConfigurationService.savePlaylists(playlistComponent.getPlaylists());
  }

  @Override
  public void addFilesToPlaylist(List<File> files) {
    List<PlaylistData> playlists = playlistComponent.addFilesToPlaylist(displayedPlaylist.getName(), files);
    appConfigurationService.savePlaylists(playlists);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
  }

  @Override
  public void deleteItemsFromPlaylist(List<Integer> itemsIndexes) {
    List<PlaylistData> playlists = playlistComponent.deleteItemsFromPlaylist(displayedPlaylist.getName(), itemsIndexes);
    appConfigurationService.savePlaylists(playlists);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
  }

  @Override
  public void playTrack(String playlistName, int trackPosition) {
    String trackPath = playlistComponent.playTrack(playlistName, trackPosition);
    playerComponent.playMedia(Paths.get(trackPath).toUri().toString());
  }

  @Override
  public void playCurrentTrack() {
    String trackPath = playlistComponent.playCurrentTrack();
    playerComponent.playMedia(Paths.get(trackPath).toUri().toString());
  }

  @Override
  public void playNextTrack() {
    String trackPath = playlistComponent.playNextTrack();
    playerComponent.playMedia(Paths.get(trackPath).toUri().toString());
    eventListeners.forEach(listener ->
        listener.changedTrackPosition(playlistComponent.getActivePlaylist().getName(),
            playlistComponent.getActiveTrackPosition())
    );
  }

  @Override
  public void playPrevTrack() {
    String trackPath = playlistComponent.playPrevTrack();
    playerComponent.playMedia(Paths.get(trackPath).toUri().toString());
    eventListeners.forEach(listener ->
        listener.changedTrackPosition(playlistComponent.getActivePlaylist().getName(),
            playlistComponent.getActiveTrackPosition())
    );
  }

  @Override
  public void stopApplication() {
    appConfigurationService.savePlaylists(playlistComponent.getPlaylists());
    playerComponent.releaseComponent();
    playlistComponent.releaseComponent();
    eventListeners.forEach(AudioPlayerEventListener::stopAudioPlayer);
  }
}
