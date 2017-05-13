package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.core.services.AppConfigurationService;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
  @Autowired
  private MediaInfoDataLoaderService mediaInfoDataLoaderService;

  private MediaInfoData currentTrackInfoData;


  public DefaultAudioPlayerFacade() {
    currentTrackInfoData = new MediaInfoData();
    eventListeners = new ArrayList<>();
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");
    refreshPlaylists();
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
  public void refreshPlaylists() {
    List<PlaylistData> playlists = appConfigurationService.getPlaylists();
    String activePlaylistName = appConfigurationService.getActivePlaylistName();
    String displayedPlaylistName = appConfigurationService.getDisplayedPlaylistName();
    playlistComponent.loadPlaylists(playlists, activePlaylistName, displayedPlaylistName);
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
  public PlaylistData getDisplayedPlaylist() {
    return playlistComponent.getDisplayedPlaylist();
  }

  @Override
  public PlaylistData getPlaylist(String playlistName) {
    return playlistComponent.getPlaylist(playlistName);
  }

  @Override
  public PlaylistData showPlaylist(String playlistName) {
    boolean displayedResult = playlistComponent.setDisplayedPlaylist(playlistName);
    if (displayedResult) {
      appConfigurationService.saveDisplayedPlaylist(playlistName);
    }
    return playlistComponent.getDisplayedPlaylist();
  }

  @Override
  public PlaylistData showActivePlaylist() {
    PlaylistData activePlaylist = getActivePlaylist();
    playlistComponent.setDisplayedPlaylist(activePlaylist);
    appConfigurationService.saveDisplayedPlaylist(activePlaylist.getName());
    return activePlaylist;
  }

  @Override
  public PlaylistData createNewPlaylist() {
    PlaylistData newPlaylist = playlistComponent.createNewPlaylist();
    eventListeners.forEach(listener -> listener.createdNewPlaylist(newPlaylist));
    appConfigurationService.savePlaylists(playlistComponent.getPlaylists(),
        playlistComponent.getActivePlaylist().getName(),
        playlistComponent.getDisplayedPlaylist().getName());
    return newPlaylist;
  }

  @Override
  public boolean deletePlaylist(String playlistName) {
    PlaylistData requiredPlaylist = getPlaylist(playlistName);
    boolean deleteDisplayed = playlistComponent.getDisplayedPlaylist().equals(requiredPlaylist);
    boolean deleteResult = playlistComponent.deletePlaylist(playlistName);
    if (deleteResult) {
      if (deleteDisplayed) {
        eventListeners.forEach(listener -> listener.changedPlaylist(playlistComponent.getDisplayedPlaylist()));
      }
      appConfigurationService.savePlaylists(playlistComponent.getPlaylists(),
          playlistComponent.getActivePlaylist().getName(),
          playlistComponent.getDisplayedPlaylist().getName());
    }
    return deleteResult;
  }

  @Override
  public void renamePlaylist(String actualPlaylistName, String newPlaylistName) {
    playlistComponent.renamePlaylist(actualPlaylistName, newPlaylistName);
    appConfigurationService.savePlaylists(playlistComponent.getPlaylists(),
        playlistComponent.getActivePlaylist().getName(),
        playlistComponent.getDisplayedPlaylist().getName());
  }

  @Override
  public void addFilesToPlaylist(List<File> files) {
    PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
    List<PlaylistData> playlists = playlistComponent.addFilesToPlaylist(displayedPlaylist.getName(), files);
    appConfigurationService.savePlaylists(playlists);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
  }

  @Override
  public void deleteItemsFromPlaylist(List<Integer> itemsIndexes) {
    PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
    List<PlaylistData> playlists = playlistComponent.deleteItemsFromPlaylist(displayedPlaylist.getName(), itemsIndexes);
    appConfigurationService.savePlaylists(playlists);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
  }

  @Override
  public void addLocationsToPlaylist(List<String> locations) {
    PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
    List<PlaylistData> playlists = playlistComponent.addLocationsToPlaylist(displayedPlaylist.getName(), locations);
    appConfigurationService.savePlaylists(playlists);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
  }

  @Override
  public void playTrack(String playlistName, int trackPosition) {
    MediaInfoData trackInfo = playlistComponent.playTrack(playlistName, trackPosition);
    playTrack(trackInfo);
  }

  @Override
  public void playCurrentTrack() {
    MediaInfoData trackInfo = playlistComponent.playCurrentTrack();
    playTrack(trackInfo);
  }

  @Override
  public void playNextTrack() {
    MediaInfoData trackInfo = playlistComponent.playNextTrack();
    playTrack(trackInfo);
  }

  @Override
  public void playPrevTrack() {
    MediaInfoData trackInfo = playlistComponent.playPrevTrack();
    playTrack(trackInfo);
  }

  private void playTrack(MediaInfoData trackInfo) {
    String resourceUri = MediaSourceType.FILE.equals(trackInfo.getSourceType()) ?
        Paths.get(trackInfo.getTrackPath()).toString() : trackInfo.getTrackPath();
    playerComponent.playMedia(resourceUri);
    currentTrackInfoData = trackInfo;
    eventListeners.forEach(listener ->
        listener.changedTrackPosition(playlistComponent.getActivePlaylist().getName(),
            playlistComponent.getActiveTrackPosition())
    );
  }

  @Override
  public MediaInfoData getCurrentTrackInfo() {
    return currentTrackInfoData;
  }

  @Override
  public void stopApplication() {
    Optional.ofNullable(playlistComponent.getPlaylists()).ifPresent(appConfigurationService::savePlaylists);
    playerComponent.releaseComponent();
    playlistComponent.releaseComponent();
    eventListeners.forEach(AudioPlayerEventListener::stopAudioPlayer);
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
