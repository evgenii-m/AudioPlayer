package ru.push.caudioplayer.core.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
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
  public List<AudioTrackData> load(List<String> mediaPaths, MediaSourceType sourceType) {
    return mediaPaths.stream()
        .map(mediaPath -> load(mediaPath, sourceType))
        .collect(Collectors.toList());
  }

  @Override
  public AudioTrackData load(String mediaPath, MediaSourceType sourceType) {
    AudioTrackData audioTrackData = new AudioTrackData();

    switch (sourceType) {
      case FILE:
        MediaMeta mediaMeta = mediaPlayerFactory.getMediaMeta(mediaPath, true);
        if (mediaMeta != null) {
          fillMediaInfoFromFile(audioTrackData, mediaMeta);
          mediaMeta.release();
        } else {
          LOG.error("Media info load fails!");
        }
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStreamByDecoder(audioTrackData, mediaPath);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }

    audioTrackData.setTrackPath(mediaPath);
    audioTrackData.setSourceType(sourceType);
    return audioTrackData;
  }

  @Override
  public void fillMediaInfoFromMediaMeta(AudioTrackData audioTrackData, MediaMeta mediaMeta,
																				 MediaSourceType sourceType) {
    switch (sourceType) {
      case FILE:
        fillMediaInfoFromFile(audioTrackData, mediaMeta);
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStream(audioTrackData, mediaMeta);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }
  }

  @Override
  public void fillMediaInfoFromHttpStream(AudioTrackData audioTrackData, MediaMeta mediaMeta) {
    setMediaInfoFromStreamTitle(audioTrackData, mediaMeta.getNowPlaying(), audioTrackData.getTrackPath());
    audioTrackData.setAlbum(mediaMeta.getTitle());
  }

  @Override
  public void fillMediaInfoFromHttpStreamByDecoder(AudioTrackData audioTrackData, String streamPath) {
    try {
      IcyStreamMetaDecoder metaDecoder = new IcyStreamMetaDecoder(streamPath);
      setMediaInfoFromStreamTitle(audioTrackData, metaDecoder.getStreamTitle(), streamPath);
      audioTrackData.setAlbum(metaDecoder.getStationName());
    } catch (IOException e) {
      LOG.error("Decode meta from Icy stream fails [streamPath = " + streamPath + "]", e);
    }
  }

  @Override
  public void fillMediaInfoFromFile(AudioTrackData audioTrackData, MediaMeta mediaMeta) {
    audioTrackData.setAlbum(mediaMeta.getAlbum());
    audioTrackData.setArtist(mediaMeta.getArtist());
    audioTrackData.setDate(mediaMeta.getDate());
    audioTrackData.setLength(mediaMeta.getLength());
    audioTrackData.setTitle(mediaMeta.getTitle());
    audioTrackData.setTrackId(mediaMeta.getTrackId());
    audioTrackData.setTrackNumber(mediaMeta.getTrackNumber());
  }

  private void setMediaInfoFromStreamTitle(AudioTrackData audioTrackData, String streamTitle, String defaultTitle) {
    if (StringUtils.isNotEmpty(streamTitle)) {
      String[] ss = streamTitle.split(STREAM_TITLE_SEPARATOR);
      if (ss.length > 0) {
        if (ss.length < 1) {
          audioTrackData.setTitle(ss[0]);
        } else {
          audioTrackData.setArtist(ss[0]);
          audioTrackData.setTitle(ss[1]);
        }
      }
    } else {
      LOG.info("Empty StreamTitle obtained.");
      audioTrackData.setTitle(defaultTitle);
    }

  }
}
