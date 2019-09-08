package ru.push.caudioplayer.core.medialoader.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.playlist.model.MediaSourceType;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/25/17
 */
public class DefaultMediaInfoDataLoaderService implements MediaInfoDataLoaderService {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultMediaInfoDataLoaderService.class);

  private static final String STREAM_TITLE_SEPARATOR = " - ";

  private final CustomMediaPlayerFactory mediaPlayerFactory;

  public DefaultMediaInfoDataLoaderService(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  @Override
  public List<PlaylistTrack> load(Playlist playlist, List<String> mediaPaths, MediaSourceType sourceType) {
    return mediaPaths.stream()
        .map(mediaPath -> load(playlist, mediaPath, sourceType))
        .collect(Collectors.toList());
  }

  @Override
  public PlaylistTrack load(Playlist playlist, String mediaPath, MediaSourceType sourceType) {
		PlaylistTrack mediaData = new PlaylistTrack(playlist);

    switch (sourceType) {
      case FILE:
        MediaMeta mediaMeta = mediaPlayerFactory.getMediaMeta(mediaPath, true);
        if (mediaMeta != null) {
          fillMediaInfoFromFile(mediaData, mediaMeta);
          mediaMeta.release();
        } else {
          LOG.error("Media info load fails!");
        }
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStreamByDecoder(mediaData, mediaPath);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }

		mediaData.setTrackPath(mediaPath);
		mediaData.setSourceType(sourceType);
    return mediaData;
  }

  @Override
  public void fillMediaInfoFromMediaMeta(PlaylistTrack mediaData, MediaMeta mediaMeta,
																				 MediaSourceType sourceType) {
    switch (sourceType) {
      case FILE:
        fillMediaInfoFromFile(mediaData, mediaMeta);
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStream(mediaData, mediaMeta);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }
  }

  @Override
  public void fillMediaInfoFromHttpStream(PlaylistTrack mediaData, MediaMeta mediaMeta) {
    setMediaInfoFromStreamTitle(mediaData, mediaMeta.getNowPlaying(), mediaData.getTrackPath());
		mediaData.setAlbum(mediaMeta.getTitle());
  }

  @Override
  public void fillMediaInfoFromHttpStreamByDecoder(PlaylistTrack mediaData, String streamPath) {
    try {
      IcyStreamMetaDecoder metaDecoder = new IcyStreamMetaDecoder(streamPath);
      setMediaInfoFromStreamTitle(mediaData, metaDecoder.getStreamTitle(), streamPath);
			mediaData.setAlbum(metaDecoder.getStationName());
    } catch (IOException e) {
      LOG.error("Decode meta from Icy stream fails [streamPath = " + streamPath + "]", e);
    }
  }

  @Override
  public void fillMediaInfoFromFile(PlaylistTrack mediaData, MediaMeta mediaMeta) {
		mediaData.setAlbum(mediaMeta.getAlbum());
		mediaData.setArtist(mediaMeta.getArtist());
		mediaData.setDate(mediaMeta.getDate());
		mediaData.setLength(mediaMeta.getLength());
		mediaData.setTitle(mediaMeta.getTitle());
		mediaData.setTrackId(mediaMeta.getTrackId());
		mediaData.setTrackNumber(mediaMeta.getTrackNumber());
  }

  private void setMediaInfoFromStreamTitle(PlaylistTrack mediaData, String streamTitle, String defaultTitle) {
    if (StringUtils.isNotEmpty(streamTitle)) {
      String[] ss = streamTitle.split(STREAM_TITLE_SEPARATOR);
      if (ss.length > 0) {
        if (ss.length < 1) {
					mediaData.setTitle(ss[0]);
        } else {
					mediaData.setArtist(ss[0]);
					mediaData.setTitle(ss[1]);
        }
      }
    } else {
      LOG.info("Empty StreamTitle obtained.");
			mediaData.setTitle(defaultTitle);
    }

  }
}
