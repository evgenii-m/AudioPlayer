package ru.push.caudioplayer.core.medialoader;

import ru.push.caudioplayer.core.playlist.domain.MediaSourceType;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 4/2/17
 */
public interface MediaInfoDataLoaderService {

  List<PlaylistItem> load(Playlist playlist, List<String> mediaPaths, MediaSourceType sourceType);

	PlaylistItem load(Playlist playlist, String mediaPath, MediaSourceType sourceType);

  void fillMediaInfoFromMediaMeta(PlaylistItem mediaData, MediaMeta mediaMeta, MediaSourceType sourceType);

  void fillMediaInfoFromHttpStream(PlaylistItem mediaData, MediaMeta mediaMeta);

  void fillMediaInfoFromHttpStreamByDecoder(PlaylistItem mediaData, String streamPath);

  void fillMediaInfoFromFile(PlaylistItem mediaData, MediaMeta mediaMeta);
}
