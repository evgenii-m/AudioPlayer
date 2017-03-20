package ru.push.caudioplayer.core.mediaplayer.helpers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.model.MediaInfoModel;
import ru.push.caudioplayer.core.mediaplayer.model.MediaSourceType;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/25/17
 */
public class MediaInfoDataLoader {
  private static final Logger LOG = LoggerFactory.getLogger(MediaInfoDataLoader.class);

  private static final String STREAM_TITLE_SEPARATOR = " - ";

  private final CustomMediaPlayerFactory mediaPlayerFactory;

  public MediaInfoDataLoader(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  public List<MediaInfoModel> load(List<String> mediaPaths, MediaSourceType sourceType) {
    return mediaPaths.stream()
        .map(mediaPath -> load(mediaPath, sourceType))
        .collect(Collectors.toList());
  }

  public MediaInfoModel load(String mediaPath, MediaSourceType sourceType) {
    MediaInfoModel mediaInfoModel = new MediaInfoModel();

    switch (sourceType) {
      case FILE:
        MediaMeta mediaMeta = mediaPlayerFactory.getMediaMeta(mediaPath, true);
        if (mediaMeta != null) {
          fillMediaInfoFromFile(mediaInfoModel, mediaMeta);
          mediaMeta.release();
        } else {
          LOG.error("Media info load fails!");
        }
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStreamByDecoder(mediaInfoModel, mediaPath);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }

    mediaInfoModel.setTrackPath(mediaPath);
    mediaInfoModel.setSourceType(sourceType);
    return mediaInfoModel;
  }

  public void fillMediaInfoFromMediaMeta(MediaInfoModel mediaInfoModel, MediaMeta mediaMeta,
                                         MediaSourceType sourceType) {
    switch (sourceType) {
      case FILE:
        fillMediaInfoFromFile(mediaInfoModel, mediaMeta);
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStream(mediaInfoModel, mediaMeta);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }
  }

  public void fillMediaInfoFromHttpStream(MediaInfoModel mediaInfoModel, MediaMeta mediaMeta) {
    setMediaInfoFromStreamTitle(mediaInfoModel, mediaMeta.getNowPlaying(), mediaInfoModel.getTrackPath());
    mediaInfoModel.setAlbum(mediaMeta.getTitle());
  }

  public void fillMediaInfoFromHttpStreamByDecoder(MediaInfoModel mediaInfoModel, String streamPath) {
    try {
      IcyStreamMetaDecoder metaDecoder = new IcyStreamMetaDecoder(streamPath);
      setMediaInfoFromStreamTitle(mediaInfoModel, metaDecoder.getStreamTitle(), streamPath);
      mediaInfoModel.setAlbum(metaDecoder.getStationName());
    } catch (IOException e) {
      LOG.error("Decode meta from Icy stream fails [streamPath = " + streamPath + "]", e);
    }
  }

  public void fillMediaInfoFromFile(MediaInfoModel mediaInfoModel, MediaMeta mediaMeta) {
    mediaInfoModel.setAlbum(mediaMeta.getAlbum());
    mediaInfoModel.setArtist(mediaMeta.getArtist());
    mediaInfoModel.setDate(mediaMeta.getDate());
    mediaInfoModel.setLength(mediaMeta.getLength());
    mediaInfoModel.setTitle(mediaMeta.getTitle());
    mediaInfoModel.setTrackId(mediaMeta.getTrackId());
    mediaInfoModel.setTrackNumber(mediaMeta.getTrackNumber());
  }

  private void setMediaInfoFromStreamTitle(MediaInfoModel mediaInfoModel, String streamTitle, String defaultTitle) {
    if (StringUtils.isNotEmpty(streamTitle)) {
      String[] ss = streamTitle.split(STREAM_TITLE_SEPARATOR);
      if (ss.length > 0) {
        if (ss.length < 1) {
          mediaInfoModel.setTitle(ss[0]);
        } else {
          mediaInfoModel.setArtist(ss[0]);
          mediaInfoModel.setTitle(ss[1]);
        }
      }
    } else {
      LOG.info("Empty StreamTitle obtained.");
      mediaInfoModel.setTitle(defaultTitle);
    }

  }
}
