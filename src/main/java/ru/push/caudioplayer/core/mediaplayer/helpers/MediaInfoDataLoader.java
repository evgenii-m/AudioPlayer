package ru.push.caudioplayer.core.mediaplayer.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaSourceType;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/25/17
 */
public class MediaInfoDataLoader {
  private static final Logger LOG = LoggerFactory.getLogger(MediaInfoDataLoader.class);

  private final CustomMediaPlayerFactory mediaPlayerFactory;

  public MediaInfoDataLoader(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  public List<MediaInfoData> load(List<String> mediaPaths, MediaSourceType sourceType) {
    return mediaPaths.stream()
        .map(mediaPath -> load(mediaPath, sourceType))
        .collect(Collectors.toList());
  }

  public MediaInfoData load(String mediaPath, MediaSourceType sourceType) {
    MediaInfoData mediaInfoData = new MediaInfoData();

    switch (sourceType) {
      case FILE:
        fillMediaInfoFromFile(mediaInfoData, mediaPath);
        break;

      case HTTP_STREAM:
        fillMediaInfoFromHttpStream(mediaInfoData, mediaPath);
        break;

      default:
        LOG.error("Unsupported media source type");
        break;
    }

    mediaInfoData.setTrackPath(mediaPath);
    mediaInfoData.setSourceType(sourceType);
    return mediaInfoData;
  }

  private void fillMediaInfoFromHttpStream(MediaInfoData mediaInfoData, String streamPath) {
    try {
      IcyStreamMetaDecoder metaDecoder = new IcyStreamMetaDecoder(streamPath);
      mediaInfoData.setArtist(metaDecoder.getArtist());
      mediaInfoData.setTitle(metaDecoder.getTitle());
      mediaInfoData.setAlbum(metaDecoder.getStationName());
    } catch (IOException e) {
      LOG.error("Decode meta from Icy stream fails [streamPath = " + streamPath + "]", e);
    }
  }

  private void fillMediaInfoFromFile(MediaInfoData mediaInfoData, String mediaPath) {
    MediaMeta mediaMeta = mediaPlayerFactory.getMediaMeta(mediaPath, true);
    if (mediaMeta != null) {
      mediaInfoData.setAlbum(mediaMeta.getAlbum());
      mediaInfoData.setArtist(mediaMeta.getArtist());
      mediaInfoData.setDate(mediaMeta.getDate());
      mediaInfoData.setLength(mediaMeta.getLength());
      mediaInfoData.setTitle(mediaMeta.getTitle());
      mediaInfoData.setTrackId(mediaMeta.getTrackId());
      mediaInfoData.setTrackNumber(mediaMeta.getTrackNumber());
      mediaMeta.release();
    } else {
      LOG.error("Media info load fails!");
    }
  }
}
