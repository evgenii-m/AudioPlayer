package ru.push.caudioplayer.core.mediaplayer.components.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.helpers.MediaInfoDataLoader;
import uk.co.caprica.vlcj.medialist.MediaList;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public class DefaultCustomPlaylistComponent implements CustomPlaylistComponent {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultCustomPlaylistComponent.class);

  private final CustomMediaPlayerFactory mediaPlayerFactory;
  private final MediaList mediaList;

  @Resource
  private MediaInfoDataLoader mediaInfoDataLoader;

  private PlaylistData playlistData;

  public DefaultCustomPlaylistComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
    this.mediaList = mediaPlayerFactory.newMediaList();
  }

  public final MediaList getMediaList() {
    return mediaList;
  }

  @Override
  public void releaseComponent() {
    LOG.debug("releaseComponent");
    mediaList.release();
  }

  @Override
  public PlaylistData loadPlaylist(PlaylistData playlistData) {
    this.playlistData = playlistData;

    this.playlistData.setTracks(
        mediaInfoDataLoader.load(
            playlistData.getTracks().stream()
                .map(MediaInfoData::getTrackPath)
                .collect(Collectors.toList())
        )
    );
    return this.playlistData;
  }

  @Override
  public PlaylistData getPlaylist() {
    return playlistData;
  }
}
