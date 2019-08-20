package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.mediaplayer.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.domain.PlaylistData;
import ru.push.caudioplayer.core.services.AppConfigurationService;
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
  private AppConfigurationService appConfigurationService;
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
    String activePlaylistUid = appConfigurationService.getActivePlaylistUid();
    String displayedPlaylistUid = appConfigurationService.getDisplayedPlaylistUid();
    boolean loadStatus = playlistComponent.loadPlaylists(playlists, activePlaylistUid, displayedPlaylistUid);

    if (!loadStatus) {
      appConfigurationService.saveAllPlaylists(
          playlistComponent.getPlaylists(),
          playlistComponent.getActivePlaylist(),
          playlistComponent.getDisplayedPlaylist()
      );
    }
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
  public PlaylistData getPlaylist(String playlistUid) {
    return playlistComponent.getPlaylist(playlistUid);
  }

  @Override
  public PlaylistData showPlaylist(String playlistUid) {
    boolean displayedResult = playlistComponent.setDisplayedPlaylist(playlistUid);
    if (displayedResult) {
      appConfigurationService.saveDisplayedPlaylist(playlistComponent.getDisplayedPlaylist());
    }
    return playlistComponent.getDisplayedPlaylist();
  }

  @Override
  public PlaylistData showActivePlaylist() {
    PlaylistData activePlaylist = getActivePlaylist();
    playlistComponent.setDisplayedPlaylist(activePlaylist);
    appConfigurationService.saveDisplayedPlaylist(activePlaylist);
    return activePlaylist;
  }

  @Override
  public PlaylistData createNewPlaylist() {
    PlaylistData newPlaylist = playlistComponent.createNewPlaylist();
    eventListeners.forEach(listener -> listener.createdNewPlaylist(newPlaylist));
    appConfigurationService.savePlaylist(newPlaylist);
    return newPlaylist;
  }

  @Override
  public boolean deletePlaylist(String playlistUid) {
    PlaylistData requiredPlaylist = getPlaylist(playlistUid);
    boolean deleteDisplayed = playlistComponent.getDisplayedPlaylist().equals(requiredPlaylist);

    if (playlistComponent.getPlaylists().size() == 1) {
      PlaylistData newPlaylist = playlistComponent.createNewPlaylist();
      appConfigurationService.savePlaylist(newPlaylist);
    }

    boolean deleteResult = playlistComponent.deletePlaylist(playlistUid);
    if (deleteResult) {
      if (deleteDisplayed) {
        eventListeners.forEach(listener -> listener.changedPlaylist(playlistComponent.getDisplayedPlaylist()));
      }
      appConfigurationService.deletePlaylist(requiredPlaylist);
    }
    return deleteResult;
  }

  @Override
  public void renamePlaylist(String playlistUid, String newPlaylistName) {
    PlaylistData changedPlaylist = playlistComponent.renamePlaylist(playlistUid, newPlaylistName);
    appConfigurationService.renamePlaylist(changedPlaylist);
    eventListeners.forEach(listener -> listener.renamedPlaylist(changedPlaylist));
  }

  @Override
  public void addFilesToPlaylist(List<File> files) {
    PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
    PlaylistData changedPlaylist = playlistComponent.addFilesToPlaylist(displayedPlaylist.getUid(), files);
    appConfigurationService.savePlaylist(changedPlaylist);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
  }

  @Override
  public void deleteItemsFromPlaylist(List<Integer> itemsIndexes) {
    PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
    PlaylistData changedPlaylist = playlistComponent.deleteItemsFromPlaylist(displayedPlaylist.getUid(), itemsIndexes);
    appConfigurationService.savePlaylist(changedPlaylist);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
  }

  @Override
  public void addLocationsToPlaylist(List<String> locations) {
    PlaylistData displayedPlaylist = playlistComponent.getDisplayedPlaylist();
    PlaylistData changedPlaylist = playlistComponent.addLocationsToPlaylist(displayedPlaylist.getUid(), locations);
    appConfigurationService.savePlaylist(changedPlaylist);
    eventListeners.forEach(listener -> listener.changedPlaylist(displayedPlaylist));
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
    appConfigurationService.saveAllPlaylists(
        playlistComponent.getPlaylists(),
        playlistComponent.getActivePlaylist(),
        playlistComponent.getDisplayedPlaylist()
    );
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
