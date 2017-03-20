package ru.push.caudioplayer.core.mediaplayer.components.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.model.MediaInfoModel;
import ru.push.caudioplayer.core.mediaplayer.model.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.model.PlaylistModel;
import ru.push.caudioplayer.core.mediaplayer.helpers.MediaInfoDataLoader;

import javax.annotation.Resource;
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

  @Resource
  private MediaInfoDataLoader mediaInfoDataLoader;

  private List<PlaylistModel> playlists;
  private PlaylistModel activePlaylist;
  private Integer trackPosition;

  public DefaultCustomPlaylistComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
  }

  @Override
  public void releaseComponent() {
    LOG.debug("releaseComponent");
  }

  private void setActivePlaylist(PlaylistModel playlist, int trackPosition) {
    this.activePlaylist = playlist;
    this.trackPosition = trackPosition;
  }


  @Override
  public void loadPlaylists(List<PlaylistModel> playlists) {
    if (CollectionUtils.isNotEmpty(playlists)) {
      this.playlists = playlists;
    } else {
      LOG.warn("Attempts to load an empty playlists!");
      this.playlists = Collections.singletonList(createNewPlaylist());
    }

    playlists.stream()
        .filter(PlaylistModel::isActive).findFirst()
        .ifPresent(activePlaylist -> setActivePlaylist(activePlaylist, 0));
  }

  @Override
  public List<PlaylistModel> getPlaylists() {
    return playlists;
  }

  @Override
  public PlaylistModel createNewPlaylist() {
    PlaylistModel newPlaylist = new PlaylistModel(playlists.size());
    playlists.add(newPlaylist);
    return newPlaylist;
  }

  @Override
  public boolean deletePlaylist(String playlistName) {
    PlaylistModel playlistModel = getPlaylist(playlistName);
    if (playlistModel != null) {
      if (playlists.size() == 1) {
        activePlaylist = createNewPlaylist();
        playlists.add(activePlaylist);
      }
      if (playlistModel.equals(activePlaylist)) {
        activePlaylist = IterableUtils.find(
            playlists, playlist -> playlist.getPosition() == (activePlaylist.getPosition() + 1)
        );
        if (activePlaylist == null) {
          activePlaylist = playlists.get(0);
        }
        trackPosition = 0;
      }
      playlists.remove(playlistModel);
      return true;
    } else {
      LOG.debug("Try delete unknown playlist [playlistName: " + playlistName + "]");
      return false;
    }
  }

  @Override
  public void renamePlaylist(String actualPlaylistName, String newPlaylistName) {

  }

  @Override
  public PlaylistModel getActivePlaylist() {
    return activePlaylist;
  }

  @Override
  public PlaylistModel getPlaylist(String playlistName) {
    return playlists.stream()
        .filter(playlist -> playlist.getName().equals(playlistName)).findFirst()
        .orElse(playlists.get(0));
  }

  @Override
  public int getActiveTrackPosition() {
    return trackPosition;
  }

  @Override
  public MediaInfoModel playTrack(String playlistName, int trackPosition) {
    activePlaylist = playlists.stream()
        .filter(playlist -> playlist.getName().equals(playlistName)).findFirst()
        .orElse(activePlaylist);
    this.trackPosition = trackPosition;
    return playCurrentTrack();
  }

  @Override
  public MediaInfoModel playCurrentTrack() {
    if ((activePlaylist == null) || CollectionUtils.isEmpty(activePlaylist.getTracks())) {
      LOG.info("Attempt to play empty or null playlist");
      return new MediaInfoModel();
    }

    if ((trackPosition < 0) || (trackPosition >= activePlaylist.getTracks().size())) {
      LOG.error("Incorrect track position [" + trackPosition + "], will be reset to 0.");
      trackPosition = 0;
    }
    return activePlaylist.getTracks().get(trackPosition);
  }

  @Override
  public MediaInfoModel playNextTrack() {
    if (trackPosition < (activePlaylist.getTracks().size() - 1)) {
      trackPosition++;
    } else {
      trackPosition = 0;
    }
    return playCurrentTrack();
  }

  @Override
  public MediaInfoModel playPrevTrack() {
    if (trackPosition > 0) {
      trackPosition--;
    } else {
      trackPosition = activePlaylist.getTracks().size() - 1;
    }
    return playCurrentTrack();
  }

  @Override
  public List<PlaylistModel> addFilesToPlaylist(String playlistName, List<File> files) {
    PlaylistModel playlist = getPlaylist(playlistName);
    List<String> mediaPaths = files.stream()
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());
    List<MediaInfoModel> mediaInfoList = mediaInfoDataLoader.load(mediaPaths, MediaSourceType.FILE);
    playlist.getTracks().addAll(mediaInfoList);
    return getPlaylists();
  }

  @Override
  public List<PlaylistModel> deleteItemsFromPlaylist(String playlistName, List<Integer> itemsIndexes) {
    PlaylistModel playlist = getPlaylist(playlistName);
    List<MediaInfoModel> deletedItems = itemsIndexes.stream()
        .filter(itemIndex -> (itemIndex >= 0) && (itemIndex < playlist.getTracks().size()))
        .map(itemIndex -> playlist.getTracks().get(itemIndex))
        .collect(Collectors.toList());
    playlist.getTracks().removeAll(deletedItems);
    return getPlaylists();
  }

  @Override
  public List<PlaylistModel> addLocationsToPlaylist(String playlistName, List<String> locations) {
    PlaylistModel playlist = getPlaylist(playlistName);
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
    List<MediaInfoModel> mediaInfoList = mediaInfoDataLoader.load(mediaPaths, MediaSourceType.HTTP_STREAM);
    playlist.getTracks().addAll(mediaInfoList);
    return getPlaylists();
  }

}
