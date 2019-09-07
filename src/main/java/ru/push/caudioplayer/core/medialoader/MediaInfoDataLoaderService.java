package ru.push.caudioplayer.core.medialoader;

import ru.push.caudioplayer.core.playlist.domain.MediaSourceType;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistTrack;
import uk.co.caprica.vlcj.player.MediaMeta;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 4/2/17
 */
public interface MediaInfoDataLoaderService {

  List<PlaylistTrack> load(Playlist playlist, List<String> mediaPaths, MediaSourceType sourceType);

	PlaylistTrack load(Playlist playlist, String mediaPath, MediaSourceType sourceType);

  void fillMediaInfoFromMediaMeta(PlaylistTrack mediaData, MediaMeta mediaMeta, MediaSourceType sourceType);

  void fillMediaInfoFromHttpStream(PlaylistTrack mediaData, MediaMeta mediaMeta);

  void fillMediaInfoFromHttpStreamByDecoder(PlaylistTrack mediaData, String streamPath);

  void fillMediaInfoFromFile(PlaylistTrack mediaData, MediaMeta mediaMeta);
}
