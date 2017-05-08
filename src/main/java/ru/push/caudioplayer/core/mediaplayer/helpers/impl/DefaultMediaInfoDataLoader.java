package ru.push.caudioplayer.core.mediaplayer.helpers.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.helpers.MediaInfoDataLoader;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/25/17
 */
public class DefaultMediaInfoDataLoader implements MediaInfoDataLoader {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultMediaInfoDataLoader.class);

  private static final String STREAM_TITLE_SEPARATOR = " - ";

  private final CustomMediaPlayerFactory mediaPlayerFactory;

  public DefaultMediaInfoDataLoader(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  @Override
  public List<MediaInfoData> load(List<String> mediaPaths, MediaSourceType sourceType) {
    return mediaPaths.stream()
        .map(mediaPath -> load(mediaPath, sourceType))
        .collect(Collectors.toList());
  }

  @Override
  public MediaInfoData load(String mediaPath, MediaSourceType sourceType) {
    MediaInfoData mediaInfoData = new MediaInfoData();

    switch (sourceType) {
      case FILE:
        MediaMeta mediaMeta = mediaPlayerFactory.getMediaMeta(mediaPath, true);
        if (mediaMeta != null) {
          fillMediaInfoFromFile(mediaInfoData, mediaMeta);
          mediaMeta.release();
        } else {
          LOG.error("Media info load fails!");
        }
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStreamByDecoder(mediaInfoData, mediaPath);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }

    mediaInfoData.setTrackPath(mediaPath);
    mediaInfoData.setSourceType(sourceType);
    return mediaInfoData;
  }

  @Override
  public void fillMediaInfoFromMediaMeta(MediaInfoData mediaInfoData, MediaMeta mediaMeta,
                                         MediaSourceType sourceType) {
    switch (sourceType) {
      case FILE:
        fillMediaInfoFromFile(mediaInfoData, mediaMeta);
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStream(mediaInfoData, mediaMeta);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }
  }

  @Override
  public void fillMediaInfoFromHttpStream(MediaInfoData mediaInfoData, MediaMeta mediaMeta) {
    setMediaInfoFromStreamTitle(mediaInfoData, mediaMeta.getNowPlaying(), mediaInfoData.getTrackPath());
    mediaInfoData.setAlbum(mediaMeta.getTitle());
  }

  @Override
  public void fillMediaInfoFromHttpStreamByDecoder(MediaInfoData mediaInfoData, String streamPath) {
    try {
      IcyStreamMetaDecoder metaDecoder = new IcyStreamMetaDecoder(streamPath);
      setMediaInfoFromStreamTitle(mediaInfoData, metaDecoder.getStreamTitle(), streamPath);
      mediaInfoData.setAlbum(metaDecoder.getStationName());
    } catch (IOException e) {
      LOG.error("Decode meta from Icy stream fails [streamPath = " + streamPath + "]", e);
    }
  }

  @Override
  public void fillMediaInfoFromFile(MediaInfoData mediaInfoData, MediaMeta mediaMeta) {
    mediaInfoData.setAlbum(mediaMeta.getAlbum());
    mediaInfoData.setArtist(mediaMeta.getArtist());
    mediaInfoData.setDate(mediaMeta.getDate());
    mediaInfoData.setLength(mediaMeta.getLength());
    mediaInfoData.setTitle(mediaMeta.getTitle());
    mediaInfoData.setTrackId(mediaMeta.getTrackId());
    mediaInfoData.setTrackNumber(mediaMeta.getTrackNumber());
  }

  private void setMediaInfoFromStreamTitle(MediaInfoData mediaInfoData, String streamTitle, String defaultTitle) {
    if (StringUtils.isNotEmpty(streamTitle)) {
      String[] ss = streamTitle.split(STREAM_TITLE_SEPARATOR);
      if (ss.length > 0) {
        if (ss.length < 1) {
          mediaInfoData.setTitle(ss[0]);
        } else {
          mediaInfoData.setArtist(ss[0]);
          mediaInfoData.setTitle(ss[1]);
        }
      }
    } else {
      LOG.info("Empty StreamTitle obtained.");
      mediaInfoData.setTitle(defaultTitle);
    }

  }
}
