package ru.push.caudioplayer.core.mediaplayer.components.impl;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.helpers.MediaInfoDataLoader;

import javax.annotation.Resource;
import java.util.Collections;
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

  private List<PlaylistData> playlists;
  private PlaylistData activePlaylist;
  private Integer trackPosition;

  public DefaultCustomPlaylistComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  @Override
  public void releaseComponent() {
    LOG.debug("releaseComponent");
  }

  private void setActivePlaylist(PlaylistData playlist, int trackPosition) {
    this.activePlaylist = playlist;

    this.activePlaylist.setTracks(
        mediaInfoDataLoader.load(
            activePlaylist.getTracks().stream()
                .map(MediaInfoData::getTrackPath)
                .collect(Collectors.toList())
        )
    );
    this.trackPosition = trackPosition;
  }


  @Override
  public void loadPlaylists(List<PlaylistData> playlists) {
    if (CollectionUtils.isNotEmpty(playlists)) {
      this.playlists = playlists;
    } else {
      LOG.warn("Attempts to load an empty playlists!");
      this.playlists = Collections.singletonList(createNewPlaylist());
    }

    playlists.stream()
        .filter(PlaylistData::isActive).findFirst()
        .ifPresent(activePlaylist -> setActivePlaylist(activePlaylist, 0));
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    return playlists;
  }

  @Override
  public PlaylistData createNewPlaylist() {
    PlaylistData newPlaylist = new PlaylistData(playlists.size());
    playlists.add(newPlaylist);
    return newPlaylist;
  }

  @Override
  public PlaylistData getActivePlaylist() {
    return activePlaylist;
  }

  @Override
  public PlaylistData getPlaylist(String playlistName) {
    return playlists.stream()
        .filter(playlist -> playlist.getName().equals(playlistName)).findFirst()
        .orElse(playlists.get(0));
  }

  @Override
  public int getActiveTrackPosition() {
    return trackPosition;
  }

  @Override
  public String playTrack(String playlistName, int trackPosition) {
    activePlaylist = playlists.stream()
        .filter(playlist -> playlist.getName().equals(playlistName)).findFirst()
        .orElse(activePlaylist);
    this.trackPosition = trackPosition;
    return playCurrentTrack();
  }

  @Override
  public String playCurrentTrack() {
    return activePlaylist.getTracks().get(trackPosition).getTrackPath();
  }

  @Override
  public String playNextTrack() {
    if (trackPosition < (activePlaylist.getTracks().size() - 1)) {
      trackPosition++;
    } else {
      trackPosition = 0;
    }
    return playCurrentTrack();
  }

  @Override
  public String playPrevTrack() {
    if (trackPosition > 0) {
      trackPosition--;
    } else {
      trackPosition = activePlaylist.getTracks().size() - 1;
    }
    return playCurrentTrack();
  }
}
