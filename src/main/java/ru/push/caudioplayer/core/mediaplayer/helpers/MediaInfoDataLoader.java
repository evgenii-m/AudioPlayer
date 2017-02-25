package ru.push.caudioplayer.core.mediaplayer.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import uk.co.caprica.vlcj.player.MediaMeta;

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

  public List<MediaInfoData> load(List<String> mediaPaths) {
    return mediaPaths.stream()
        .map(mediaPath -> {
          MediaInfoData mediaInfoData = new MediaInfoData();
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
          }
          mediaInfoData.setTrackPath(mediaPath);
          return mediaInfoData;
        }).collect(Collectors.toList());
  }
}
