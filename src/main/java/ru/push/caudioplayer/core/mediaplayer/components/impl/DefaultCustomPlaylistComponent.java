package ru.push.caudioplayer.core.mediaplayer.components.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
  public boolean loadPlaylists(List<PlaylistData> playlists, String activePlaylistUid, String displayedPlaylistUid) {
    if (CollectionUtils.isNotEmpty(playlists)) {
      this.playlists = playlists;
      boolean activeResult = setActivePlaylist(activePlaylistUid, 0);
      if (!activeResult) {
        activePlaylist = playlists.get(0);
        trackPosition = 0;
      }
      boolean displayedResult = setDisplayedPlaylist(displayedPlaylistUid);
      if (!displayedResult) {
        displayedPlaylist = playlists.get(0);
      }
      return activeResult && displayedResult;

    } else {
			this.playlists = new ArrayList<>();
			LOG.warn("Attempts to load an empty playlists!");
			return false;
    }

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
  public boolean deletePlaylist(String playlistUid) {
    if (playlists.size() == 1) {
      LOG.warn("Can not delete last playlist!");
      return false;
    }

    PlaylistData playlistData = getPlaylist(playlistUid);
    if (playlistData == null) {
      LOG.debug("Try delete unknown playlist [playlistUid: " + playlistUid + "]");
      return false;
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

  }

  @Override
  public PlaylistData renamePlaylist(String playlistUid, String newPlaylistName) {
    Assert.notNull(playlistUid);
    Assert.notNull(newPlaylistName);

    PlaylistData playlistData = getPlaylist(playlistUid);
    if (playlistData == null) {
      LOG.info("Playlist with name '" + playlistUid + "' not found, rename failed.");
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
  public PlaylistData getPlaylist(String playlistUid) {
    return IterableUtils.find(
        playlists, playlist -> playlist.getUid().equals(playlistUid)
    );
  }

  private boolean setActivePlaylist(String playlistUid, int trackPosition) {
    PlaylistData requiredPlaylist = getPlaylist(playlistUid);
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
      LOG.error("Attempts to activate a playlist that not found in component [playlistUid = " + playlistUid + "]");
      return false;
    }
  }

  @Override
  public boolean setDisplayedPlaylist(String playlistUid) {
    PlaylistData requiredPlaylist = getPlaylist(playlistUid);
    if (requiredPlaylist != null) {
      displayedPlaylist = requiredPlaylist;
      return true;
    } else {
      LOG.error("Attempts to display a playlist that not found in component [playlistUid = " + playlistUid + "]");
      return false;
    }
  }

  @Override
  public void setDisplayedPlaylist(PlaylistData playlist) {
    Assert.notNull(playlist);
    displayedPlaylist = playlist;
  }

  @Override
  public int getActiveTrackPosition() {
    return trackPosition;
  }

  @Override
  public AudioTrackData playTrack(String playlistUid, int trackPosition) throws IllegalArgumentException {
    boolean activeResult = setActivePlaylist(playlistUid, trackPosition);
    if (!activeResult) {
      throw new IllegalArgumentException("Invalid arguments [playlistUid = " + playlistUid
          + ", trackPosition = " + trackPosition + "]");
    }
    return playCurrentTrack();
  }

  @Override
  public AudioTrackData playCurrentTrack() {
    if ((activePlaylist == null) || CollectionUtils.isEmpty(activePlaylist.getTracks())) {
      LOG.info("Attempt to play empty or null playlist");
      return new AudioTrackData();
    }

    if ((trackPosition < 0) || (trackPosition >= activePlaylist.getTracks().size())) {
      LOG.error("Incorrect track position [" + trackPosition + "], will be reset to 0.");
      trackPosition = 0;
    }
    return activePlaylist.getTracks().get(trackPosition);
  }

  @Override
  public AudioTrackData playNextTrack() {
    if (trackPosition < (activePlaylist.getTracks().size() - 1)) {
      trackPosition++;
    } else {
      trackPosition = 0;
    }
    return playCurrentTrack();
  }

  @Override
  public AudioTrackData playPrevTrack() {
    if (trackPosition > 0) {
      trackPosition--;
    } else {
      trackPosition = activePlaylist.getTracks().size() - 1;
    }
    return playCurrentTrack();
  }

  @Override
  public PlaylistData addFilesToPlaylist(String playlistUid, List<File> files) {
    PlaylistData playlist = getPlaylist(playlistUid);
    List<String> mediaPaths = files.stream()
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());
    List<AudioTrackData> mediaInfoList = mediaInfoDataLoaderService.load(mediaPaths, MediaSourceType.FILE);
    playlist.getTracks().addAll(mediaInfoList);
    return playlist;
  }

  @Override
  public PlaylistData deleteItemsFromPlaylist(String playlistUid, List<Integer> itemsIndexes) {
    PlaylistData playlist = getPlaylist(playlistUid);
    itemsIndexes.stream()
        .filter(itemIndex -> (itemIndex >= 0) && (itemIndex < playlist.getTracks().size()))
        .forEach(itemIndex -> playlist.getTracks().remove(itemIndex.intValue()));
    return playlist;
  }

  @Override
  public PlaylistData addLocationsToPlaylist(String playlistUid, List<String> locations) {
    PlaylistData playlist = getPlaylist(playlistUid);
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
    List<AudioTrackData> mediaInfoList = mediaInfoDataLoaderService.load(mediaPaths, MediaSourceType.HTTP_STREAM);
    playlist.getTracks().addAll(mediaInfoList);
    return playlist;
  }

}
