package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.playlist.domain.MediaSourceType;
import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

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

  @Autowired
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
	private PlaylistService playlistService;
  @Autowired
  private MediaInfoDataLoaderService mediaInfoDataLoaderService;
	@Autowired
	private DtoMapper dtoMapper;


  public DefaultAudioPlayerFacade() {
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
  public void playTrack(String playlistUid, int trackIndex) {
		PlaylistItem playlistTrack = playlistService.setActivePlaylistTrack(playlistUid, trackIndex);
    playTrack(playlistTrack);
  }

  @Override
  public void playCurrentTrack() {
		PlaylistItem playlistTrack = playlistService.getActivePlaylistTrack();
		playTrack(playlistTrack);
  }

  @Override
  public void playNextTrack() {
		PlaylistItem playlistTrack = playlistService.nextActivePlaylistTrack();
		playTrack(playlistTrack);
  }

  @Override
  public void playPrevTrack() {
		PlaylistItem playlistTrack = playlistService.prevActivePlaylistTrack();
		playTrack(playlistTrack);
  }

  private void playTrack(PlaylistItem playlistTrack) {
    String resourceUri = MediaSourceType.FILE.equals(playlistTrack.getSourceType()) ?
        Paths.get(playlistTrack.getTrackPath()).toString() : playlistTrack.getTrackPath();
    playerComponent.playMedia(resourceUri);
    eventListeners.forEach(listener ->{
    	Playlist playlist = playlistTrack.getPlaylist();
			PlaylistData playlistData = dtoMapper.mapPlaylistData(playlist);
			listener.changedTrackPosition(playlistData, playlist.getItems().indexOf(playlistTrack));
    });
  }

  @Override
  public TrackData getActivePlaylistTrack() {
    return dtoMapper.mapTrackData(playlistService.getActivePlaylistTrack());
  }

	@Override
  public void stopApplication() {
    playerComponent.releaseComponent();
  }

  private class AudioPlayerFacadeEventListener extends MediaPlayerEventAdapter {

    @Override
    public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
      if (mediaPlayer.isPlaying()) {  // media changes actual only when playing media
        LOG.debug("mediaMetaChanged");
				PlaylistItem playlistTrack = playlistService.getActivePlaylistTrack();
				if (playlistTrack != null) {
					MediaMeta mediaMeta = mediaPlayer.getMediaMeta();
					if (mediaMeta != null) {
						MediaSourceType sourceType = (playlistTrack.getSourceType() != null) ?
								playlistTrack.getSourceType() : MediaSourceType.FILE;
						mediaInfoDataLoaderService.fillMediaInfoFromMediaMeta(playlistTrack, mediaMeta, sourceType);
						mediaMeta.release();
					} else {
						LOG.error("Media info is null!");
					}
				}
      }
    }
  }
}
