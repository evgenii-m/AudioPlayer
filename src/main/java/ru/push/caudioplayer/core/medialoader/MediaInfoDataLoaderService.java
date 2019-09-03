package ru.push.caudioplayer.core.medialoader;

import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 4/2/17
 */
public interface MediaInfoDataLoaderService {

  List<AudioTrackData> load(List<String> mediaPaths, MediaSourceType sourceType);

  AudioTrackData load(String mediaPath, MediaSourceType sourceType);

  void fillMediaInfoFromMediaMeta(AudioTrackData audioTrackData, MediaMeta mediaMeta, MediaSourceType sourceType);

  void fillMediaInfoFromHttpStream(AudioTrackData audioTrackData, MediaMeta mediaMeta);

  void fillMediaInfoFromHttpStreamByDecoder(AudioTrackData audioTrackData, String streamPath);

  void fillMediaInfoFromFile(AudioTrackData audioTrackData, MediaMeta mediaMeta);
}
