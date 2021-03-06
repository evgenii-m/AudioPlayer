package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.facades.dto.NotificationData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.playlist.model.MediaSourceType;
import ru.push.caudioplayer.core.playlist.LocalPlaylistService;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public class DefaultAudioPlayerFacade implements AudioPlayerFacade {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAudioPlayerFacade.class);

  private final List<AudioPlayerEventListener> eventListeners;
	private final ScheduledExecutorService scrobblerExecutor = Executors.newSingleThreadScheduledExecutor();

	private ScheduledFuture<?> scrobblerScheduledFuture = null;

	@Autowired
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
	private LocalPlaylistService localPlaylistService;
  @Autowired
  private MediaInfoDataLoaderService mediaInfoDataLoaderService;
	@Autowired
	private DtoMapper dtoMapper;
	@Autowired
	private LastFmService lastFmService;


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
	public void releaseAudioPlayer() {
		playerComponent.releaseComponent();
		scrobblerExecutor.shutdownNow();
	}

	@Override
	public void resumePlayingTrack() {
		localPlaylistService.getActivePlaylistTrack().ifPresent(o -> {
			playerComponent.resume();
			startPlayingNewTrack(o);
		});
	}

  @Override
  public void playTrack(String playlistUid, String trackUid) {
		Optional<PlaylistTrack> playlistTrack = localPlaylistService.setActivePlaylistTrack(playlistUid, trackUid);
    playTrack(playlistTrack);
  }

  @Override
  public void playNextTrack() {
		Optional<PlaylistTrack> playlistTrack = localPlaylistService.nextActivePlaylistTrack();
		playTrack(playlistTrack);
  }

  @Override
	public void playPrevTrack() {
		Optional<PlaylistTrack> playlistTrack = localPlaylistService.prevActivePlaylistTrack();
		playTrack(playlistTrack);
  }

  private void playTrack(Optional<PlaylistTrack> playlistTrack) {
		if (playlistTrack.isPresent()) {
			PlaylistTrack track = playlistTrack.get();
			String resourceUri = MediaSourceType.FILE.equals(track.getSourceType()) ?
					Paths.get(track.getTrackPath()).toString() : track.getTrackPath();
			playerComponent.playMedia(resourceUri);
			startPlayingNewTrack(track);

			TrackData trackData = dtoMapper.mapTrackData(track);
			eventListeners.forEach(listener -> listener.changedNowPlayingTrack(trackData));

		} else {
			LOG.error("No track set to play.");
		}
	}

	@Override
  public Optional<TrackData> getActivePlaylistTrack() {
    return localPlaylistService.getActivePlaylistTrack().map(o -> dtoMapper.mapTrackData(o));
  }

	@Override
	public void pauseCurrentTrack() {
		localPlaylistService.getActivePlaylistTrack().ifPresent(o -> {
			playerComponent.pause();
			cancelScrobbling();
		});
	}

	@Override
	public void stopPlaying() {
		playerComponent.stop();
		localPlaylistService.getActivePlaylistTrack().ifPresent(track -> {
			localPlaylistService.resetActivePlaylistTrack();
			cancelScrobbling();
			TrackData trackData = dtoMapper.mapTrackData(track);
			eventListeners.forEach(listener -> listener.changedNowPlayingTrack(trackData));
		});
	}

	private synchronized void startPlayingNewTrack(PlaylistTrack track) {
		lastFmService.updateNowPlaying(track.getArtist(), track.getTitle(), track.getAlbum());
		cancelScrobbling();
		long taskDelay = lastFmService.calculateScrobbleDelay(track);
		scrobblerScheduledFuture = scrobblerExecutor.schedule(new ScrobblerRunnable(track), taskDelay, TimeUnit.MILLISECONDS);
	}

	private void cancelScrobbling() {
		if ((scrobblerScheduledFuture != null) && !scrobblerScheduledFuture.isDone()
				&& !scrobblerScheduledFuture.isCancelled()) {
			scrobblerScheduledFuture.cancel(true);
		}
	}

	private void sendNotification(String messageText) {
		eventListeners.forEach(l -> l.obtainedNotification(new NotificationData(messageText)));
	}

	private final class ScrobblerRunnable implements Runnable {
		private PlaylistTrack trackForScrobble;

  	private ScrobblerRunnable(PlaylistTrack trackForScrobble) {
  		this.trackForScrobble = trackForScrobble;
		}

		@Override
		public void run() {
			Date currentDate = new Date();
			boolean chosenByUser = !MediaSourceType.HTTP_STREAM.equals(trackForScrobble.getSourceType());
			boolean result = lastFmService.scrobbleTrack(trackForScrobble.getArtist(), trackForScrobble.getTitle(),
					trackForScrobble.getAlbum(), currentDate, chosenByUser);
			if (result) {
				LOG.info("Scrobbled track: track = {}, time = {}", trackForScrobble, currentDate);
			}
		}
	}

  private class AudioPlayerFacadeEventListener extends MediaPlayerEventAdapter {

    @Override
    public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
      if (mediaPlayer.isPlaying()) {  // media changes actual only when playing media
        LOG.debug("mediaMetaChanged");
				Optional<PlaylistTrack> playlistTrack = localPlaylistService.getActivePlaylistTrack();

				playlistTrack.ifPresent(track -> {
					MediaMeta mediaMeta = mediaPlayer.getMediaMeta();
					if (mediaMeta != null) {
						MediaSourceType sourceType = (track.getSourceType() != null) ?
								track.getSourceType() : MediaSourceType.FILE;
						mediaInfoDataLoaderService.fillMediaInfoFromMediaMeta(track, mediaMeta, sourceType);
						mediaMeta.release();

						startPlayingNewTrack(track);
						PlaylistData playlistData = dtoMapper.mapPlaylistData(track.getPlaylist());
						TrackData trackData = dtoMapper.mapTrackData(track);
						eventListeners.forEach(listener -> listener.changedTrackData(playlistData, trackData));

					} else {
						LOG.error("Media info is null!");
						sendNotification(NotificationMessages.TRACK_MEDIA_DATA_INCORRECT);
					}
				});
      }
    }
  }
}
