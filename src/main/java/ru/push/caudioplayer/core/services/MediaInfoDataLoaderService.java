package ru.push.caudioplayer.core.services;

import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 4/2/17
 */
public interface MediaInfoDataLoaderService {

  List<MediaInfoData> load(List<String> mediaPaths, MediaSourceType sourceType);

  MediaInfoData load(String mediaPath, MediaSourceType sourceType);

  void fillMediaInfoFromMediaMeta(MediaInfoData mediaInfoData, MediaMeta mediaMeta, MediaSourceType sourceType);

  void fillMediaInfoFromHttpStream(MediaInfoData mediaInfoData, MediaMeta mediaMeta);

  void fillMediaInfoFromHttpStreamByDecoder(MediaInfoData mediaInfoData, String streamPath);

  void fillMediaInfoFromFile(MediaInfoData mediaInfoData, MediaMeta mediaMeta);
}
