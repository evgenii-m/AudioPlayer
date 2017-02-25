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

  @Resource
  private MediaInfoDataLoader mediaInfoDataLoader;

  private PlaylistData playlistData;
  private Integer playlistPosition;

  public DefaultCustomPlaylistComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  @Override
  public void releaseComponent() {
    LOG.debug("releaseComponent");
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
    this.playlistPosition = 1;
    return this.playlistData;
  }



  @Override
  public PlaylistData getPlaylist() {
    return playlistData;
  }

  @Override
  public String getTrackPath(int position) {
    playlistPosition = position;
    return getTrackPath();
  }

  @Override
  public String getTrackPath() {
    return playlistData.getTracks().get(playlistPosition).getTrackPath();
  }

  @Override
  public String getNextTrackPath() {
    playlistPosition++;
    return getTrackPath();
  }

  @Override
  public String getPrevTrackPath() {
    playlistPosition--;
    return getTrackPath();
  }
}
