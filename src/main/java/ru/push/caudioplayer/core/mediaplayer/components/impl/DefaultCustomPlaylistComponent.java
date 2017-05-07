package ru.push.caudioplayer.core.mediaplayer.components.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.helpers.MediaInfoDataLoader;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public class DefaultCustomPlaylistComponent implements CustomPlaylistComponent {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultCustomPlaylistComponent.class);

  private final CustomMediaPlayerFactory mediaPlayerFactory;

  @Autowired
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
  public boolean deletePlaylist(String playlistName) {
    PlaylistData playlistData = getPlaylist(playlistName);
    if (playlistData != null) {
      if (playlists.size() == 1) {
        activePlaylist = createNewPlaylist();
      }
      if (playlistData.equals(activePlaylist)) {
        activePlaylist = IterableUtils.find(
            playlists, playlist -> playlist.getPosition() == (activePlaylist.getPosition() + 1)
        );
        if (activePlaylist == null) {
          activePlaylist = playlists.get(0);
        }
        trackPosition = 0;
      }
      playlists.remove(playlistData);
      return true;
    } else {
      LOG.debug("Try delete unknown playlist [playlistName: " + playlistName + "]");
      return false;
    }
  }

  @Override
  public void renamePlaylist(String actualPlaylistName, String newPlaylistName) {
    PlaylistData playlistData = IterableUtils.find(
        playlists, playlist -> actualPlaylistName.equals(playlist.getName())
    );
    if (playlistData == null) {
      LOG.info("Playlist with name '" + actualPlaylistName + "' not found, rename failed.");
      return;
    }
    // TODO: add validation for playlist name
    playlistData.setName(newPlaylistName);
  }

  @Override
  public PlaylistData getActivePlaylist() {
    return activePlaylist;
  }

  @Override
  public PlaylistData getPlaylist(String playlistName) {
    return IterableUtils.find(
        playlists, playlist -> playlist.getName().equals(playlistName)
    );
  }

  @Override
  public int getActiveTrackPosition() {
    return trackPosition;
  }

  @Override
  public MediaInfoData playTrack(String playlistName, int trackPosition) {
    activePlaylist = playlists.stream()
        .filter(playlist -> playlist.getName().equals(playlistName)).findFirst()
        .orElse(activePlaylist);
    this.trackPosition = trackPosition;
    return playCurrentTrack();
  }

  @Override
  public MediaInfoData playCurrentTrack() {
    if ((activePlaylist == null) || CollectionUtils.isEmpty(activePlaylist.getTracks())) {
      LOG.info("Attempt to play empty or null playlist");
      return new MediaInfoData();
    }

    if ((trackPosition < 0) || (trackPosition >= activePlaylist.getTracks().size())) {
      LOG.error("Incorrect track position [" + trackPosition + "], will be reset to 0.");
      trackPosition = 0;
    }
    return activePlaylist.getTracks().get(trackPosition);
  }

  @Override
  public MediaInfoData playNextTrack() {
    if (trackPosition < (activePlaylist.getTracks().size() - 1)) {
      trackPosition++;
    } else {
      trackPosition = 0;
    }
    return playCurrentTrack();
  }

  @Override
  public MediaInfoData playPrevTrack() {
    if (trackPosition > 0) {
      trackPosition--;
    } else {
      trackPosition = activePlaylist.getTracks().size() - 1;
    }
    return playCurrentTrack();
  }

  @Override
  public List<PlaylistData> addFilesToPlaylist(String playlistName, List<File> files) {
    PlaylistData playlist = getPlaylist(playlistName);
    List<String> mediaPaths = files.stream()
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());
    List<MediaInfoData> mediaInfoList = mediaInfoDataLoader.load(mediaPaths, MediaSourceType.FILE);
    playlist.getTracks().addAll(mediaInfoList);
    return getPlaylists();
  }

  @Override
  public List<PlaylistData> deleteItemsFromPlaylist(String playlistName, List<Integer> itemsIndexes) {
    PlaylistData playlist = getPlaylist(playlistName);
    itemsIndexes.stream()
        .filter(itemIndex -> (itemIndex >= 0) && (itemIndex < playlist.getTracks().size()))
        .forEach(itemIndex -> playlist.getTracks().remove(itemIndex.intValue()));
    return getPlaylists();
  }

  @Override
  public List<PlaylistData> addLocationsToPlaylist(String playlistName, List<String> locations) {
    PlaylistData playlist = getPlaylist(playlistName);
    List<String> mediaPaths = locations.stream()
        .map(location -> {
          URL locationUrl = null;
          try {
            locationUrl = new URL(location);
          } catch (MalformedURLException e) {
            LOG.info("Bad URL [" + location + "].", e);
          }
          return (locationUrl != null) ? locationUrl.toString() : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    List<MediaInfoData> mediaInfoList = mediaInfoDataLoader.load(mediaPaths, MediaSourceType.HTTP_STREAM);
    playlist.getTracks().addAll(mediaInfoList);
    return getPlaylists();
  }

}
