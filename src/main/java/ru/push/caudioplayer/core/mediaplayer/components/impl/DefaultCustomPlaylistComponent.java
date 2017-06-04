package ru.push.caudioplayer.core.mediaplayer.components.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
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
  private MediaInfoDataLoaderService mediaInfoDataLoaderService;

  private List<PlaylistData> playlists;
  private PlaylistData displayedPlaylist;
  private PlaylistData activePlaylist;
  private Integer trackPosition;

  public DefaultCustomPlaylistComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  @Override
  public void releaseComponent() {
    LOG.debug("releaseComponent");
  }

  @Override
  public boolean loadPlaylists(List<PlaylistData> playlists, String activePlaylistName, String displayedPlaylistName) {
    boolean loadStatus;   // if true - all right, if false - with errors and need refresh config file

    if (CollectionUtils.isNotEmpty(playlists)) {
      this.playlists = playlists;
      boolean activeResult = setActivePlaylist(activePlaylistName, 0);
      if (!activeResult) {
        activePlaylist = playlists.get(0);
        trackPosition = 0;
      }
      boolean displayedResult = setDisplayedPlaylist(displayedPlaylistName);
      if (!displayedResult) {
        displayedPlaylist = playlists.get(0);
      }
      loadStatus = activeResult && displayedResult;

    } else {
      LOG.warn("Attempts to load an empty playlists!");
      PlaylistData newPlaylist = createNewPlaylist();
      this.playlists = Collections.singletonList(newPlaylist);
      activePlaylist = newPlaylist;
      trackPosition = 0;
      displayedPlaylist = newPlaylist;
      loadStatus = false;
    }

    return loadStatus;
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    return playlists;
  }

  @Override
  public PlaylistData createNewPlaylist() {
    PlaylistData newPlaylist = new PlaylistData();
    playlists.add(newPlaylist);
    displayedPlaylist = newPlaylist;
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
        int playlistIndex = playlists.indexOf(activePlaylist);
        if (playlistIndex < (playlists.size() - 1)) {
          activePlaylist = playlists.get(playlistIndex + 1);
        } else {
          activePlaylist = playlists.get(0);
        }
        trackPosition = 0;
      }
      if (playlistData.equals(displayedPlaylist)) {
        displayedPlaylist = activePlaylist;
      }
      playlists.remove(playlistData);
      return true;
    } else {
      LOG.debug("Try delete unknown playlist [playlistName: " + playlistName + "]");
      return false;
    }
  }

  @Override
  public PlaylistData renamePlaylist(String actualPlaylistName, String newPlaylistName) {
    PlaylistData playlistData = IterableUtils.find(
        playlists, playlist -> actualPlaylistName.equals(playlist.getName())
    );
    if (playlistData == null) {
      LOG.info("Playlist with name '" + actualPlaylistName + "' not found, rename failed.");
      return null;
    }
    // TODO: add validation for playlist name
    playlistData.setName(newPlaylistName);
    return playlistData;
  }

  @Override
  public PlaylistData getActivePlaylist() {
    return activePlaylist;
  }

  @Override
  public PlaylistData getDisplayedPlaylist() {
    return displayedPlaylist;
  }

  @Override
  public PlaylistData getPlaylist(String playlistName) {
    return IterableUtils.find(
        playlists, playlist -> playlist.getName().equals(playlistName)
    );
  }

  private boolean setActivePlaylist(String playlistName, int trackPosition) {
    PlaylistData requiredPlaylist = IterableUtils.find(
        playlists, playlist -> playlist.getName().equals(playlistName)
    );
    if (requiredPlaylist != null) {
      int playlistSize = requiredPlaylist.getTracks().size();
      if ((trackPosition >= 0) && (trackPosition < playlistSize)) {
        activePlaylist = requiredPlaylist;
        this.trackPosition = trackPosition;
        return true;
      } else {
        LOG.error("Attempts to set invalid track position [trackPosition = " + trackPosition
            + ", playlistSize = " + playlistSize + "]");
        return false;
      }
    } else {
      LOG.error("Attempts to activate a playlist that not found in component [playlistName = " + playlistName + "]");
      return false;
    }
  }

  @Override
  public boolean setDisplayedPlaylist(String playlistName) {
    PlaylistData requiredPlaylist = IterableUtils.find(
        playlists, playlist -> playlist.getName().equals(playlistName)
    );
    if (requiredPlaylist != null) {
      displayedPlaylist = requiredPlaylist;
      return true;
    } else {
      LOG.error("Attempts to display a playlist that not found in component [playlistName = " + playlistName + "]");
      return false;
    }
  }

  @Override
  public void setDisplayedPlaylist(PlaylistData playlist) {
    displayedPlaylist = playlist;
  }

  @Override
  public int getActiveTrackPosition() {
    return trackPosition;
  }

  @Override
  public MediaInfoData playTrack(String playlistName, int trackPosition) throws IllegalArgumentException {
    boolean activeResult = setActivePlaylist(playlistName, trackPosition);
    if (!activeResult) {
      throw new IllegalArgumentException("Invalid arguments [playlistName = " + playlistName
          + ", trackPosition = " + trackPosition + "]");
    }
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
  public PlaylistData addFilesToPlaylist(String playlistName, List<File> files) {
    PlaylistData playlist = getPlaylist(playlistName);
    List<String> mediaPaths = files.stream()
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());
    List<MediaInfoData> mediaInfoList = mediaInfoDataLoaderService.load(mediaPaths, MediaSourceType.FILE);
    playlist.getTracks().addAll(mediaInfoList);
    return playlist;
  }

  @Override
  public PlaylistData deleteItemsFromPlaylist(String playlistName, List<Integer> itemsIndexes) {
    PlaylistData playlist = getPlaylist(playlistName);
    itemsIndexes.stream()
        .filter(itemIndex -> (itemIndex >= 0) && (itemIndex < playlist.getTracks().size()))
        .forEach(itemIndex -> playlist.getTracks().remove(itemIndex.intValue()));
    return playlist;
  }

  @Override
  public PlaylistData addLocationsToPlaylist(String playlistName, List<String> locations) {
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
    List<MediaInfoData> mediaInfoList = mediaInfoDataLoaderService.load(mediaPaths, MediaSourceType.HTTP_STREAM);
    playlist.getTracks().addAll(mediaInfoList);
    return playlist;
  }

}
