package ru.push.caudioplayer.core.services.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.core.services.AppConfigurationService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.push.caudioplayer.core.services.ServicesConstants.*;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public class CommonsAppConfigurationService implements AppConfigurationService {
  private static final Logger LOG = LoggerFactory.getLogger(CommonsAppConfigurationService.class);
  private static final String DEFAULT_CONFIG_FILE_NAME = "mediaplayer-app-configuration.xml";
  private static final String UNTITLED_PLAYLIST_NAME = "Untitled";

  @Autowired
  private MediaInfoDataLoaderService mediaInfoDataLoaderService;

  private final FileBasedConfigurationBuilder<XMLConfiguration> configurationBuilder;
  private XMLConfiguration configuration;

  public CommonsAppConfigurationService(String configurationFileName) {
    Parameters params = new Parameters();
    configurationBuilder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
        .configure(
            params.xml().setFileName(configurationFileName)
        );

    try {
      configuration = configurationBuilder.getConfiguration();
    } catch (ConfigurationException e) {
      LOG.error("Exception occurred when configuration load, will be created default configuration file.", e);
      createDefaultConfiguration();
    }
  }

  public CommonsAppConfigurationService() {
    this(DEFAULT_CONFIG_FILE_NAME);
  }

  private void createDefaultConfiguration() {
    configuration = new XMLConfiguration();
    configuration.addProperty(CONF_ROOT_NODE, new Object());
    try {
      configurationBuilder.save();
      configuration = configurationBuilder.getConfiguration();
    } catch (ConfigurationException e) {
      LOG.error("Exception occurred when create default configuration.", e);
    }
  }

  private ImmutableNode getConfigurationRootChildNode(String nodeName) {
    return configuration.getNodeModel().getRootNode().getChildren().stream()
        .filter(node -> node.getNodeName().equals(nodeName)).findFirst()
        .orElse(null);
  }

  @Override
  public String getActivePlaylistName() {
    return configuration.getString(ACTIVE_PLAYLIST_NAME_NODE);
  }

  @Override
  public String getDisplayedPlaylistName() {
    return configuration.getString(DISPLAYED_PLAYLIST_NAME_NODE);
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    ImmutableNode playlistsNode = getConfigurationRootChildNode(PLAYLISTS_SET_NODE);

    if (playlistsNode != null && CollectionUtils.isNotEmpty(playlistsNode.getChildren())) {
      List<PlaylistData> playlists = playlistsNode.getChildren().stream()
          .filter(playlistNode -> PLAYLIST_NODE_NAME.equals(playlistNode.getNodeName()))
          .map(playlistNode -> {
            String playlistName = (String) playlistNode.getAttributes()
                .getOrDefault(PLAYLIST_NODE_ATTR_NAME, UNTITLED_PLAYLIST_NAME);
            List<MediaInfoData> playlistTracks = playlistNode.getChildren().stream()
                .map(trackNode -> {
                  String trackPath = (String) trackNode.getValue();
                  MediaSourceType sourceType = MediaSourceType.valueOf(
                      StringUtils.upperCase(
                          (String) trackNode.getAttributes()
                              .getOrDefault(PLAYLIST_TRACK_NODE_ATTR_SOURCE_TYPE, MediaSourceType.FILE.name())
                      )
                  );
                  return mediaInfoDataLoaderService.load(trackPath, sourceType);
                }).collect(Collectors.toList());
            return new PlaylistData(playlistName, playlistTracks);
          }).collect(Collectors.toList());
      return playlists;

    } else {
      LOG.warn("Playlists block not found or empty (load operation)!");
      return null;
    }
  }

  @Override
  public void saveActivePlaylist(PlaylistData activePlaylist) throws IllegalArgumentException {
    if (activePlaylist == null) {
      throw new IllegalArgumentException("Active playlist is null, saving aborted!");
    }

    configuration.setProperty(ACTIVE_PLAYLIST_NAME_NODE, activePlaylist.getName());
    saveConfiguration();
  }

  @Override
  public void saveDisplayedPlaylist(PlaylistData displayedPlaylist) throws IllegalArgumentException {
    if (displayedPlaylist == null) {
      throw new IllegalArgumentException("Displayed playlist is null, saving aborted!");
    }

    configuration.setProperty(DISPLAYED_PLAYLIST_NAME_NODE, displayedPlaylist.getName());
    saveConfiguration();
  }

  private List<ImmutableNode> convertPlaylistTracksToNodes(List<MediaInfoData> tracks) {
    return tracks.stream()
        .map(trackData ->
            new ImmutableNode.Builder()
                .name(PLAYLIST_TRACK_NODE_NAME)
                .addAttribute(PLAYLIST_TRACK_NODE_ATTR_SOURCE_TYPE, trackData.getSourceType().name())
                .value(trackData.getTrackPath())
                .create()
        ).collect(Collectors.toList());
  }

  @Override
  public void savePlaylist(PlaylistData playlistData) throws IllegalArgumentException {
    if (playlistData == null) {
      throw new IllegalArgumentException("Playlist is null, saving aborted!");
    }

    ImmutableNode playlistsNode = getConfigurationRootChildNode(PLAYLISTS_SET_NODE);
    if ((playlistsNode != null) && CollectionUtils.isNotEmpty(playlistsNode.getChildren())) {
      int playlistIndex = IterableUtils.indexOf(
          playlistsNode.getChildren(),
          playlistNode -> playlistData.getName().equals(playlistNode.getAttributes().get(PLAYLIST_NODE_ATTR_NAME))
      );

      if (playlistIndex >= 0) {
        // changing existing playlist
        String playlistNode = PLAYLIST_NODE + "(" + playlistIndex + ")";
        configuration.addNodes(playlistNode, convertPlaylistTracksToNodes(playlistData.getTracks()));
        saveConfiguration();
        return;
      }
    }

    // creating new playlist
    ImmutableNode newPlaylistNode = new ImmutableNode.Builder()
        .name(PLAYLIST_NODE_NAME)
        .addAttribute(PLAYLIST_NODE_ATTR_NAME, playlistData.getName())
        .addChildren(convertPlaylistTracksToNodes(playlistData.getTracks()))
        .create();
    configuration.addNodes(PLAYLISTS_SET_NODE, Collections.singletonList(newPlaylistNode));
    saveConfiguration();
  }

  @Override
  public void renamePlaylist(PlaylistData playlistData) throws IllegalArgumentException {

  }

  @Override
  public void deletePlaylist(PlaylistData playlistData) throws IllegalArgumentException {

  }

  private void setPlaylistsInConfiguration(List<PlaylistData> playlistsData) throws IllegalArgumentException {
    if (playlistsData == null) {
      throw new IllegalArgumentException("Playlists is null, saving aborted!");
    }

    ImmutableNode playlistsNode = getConfigurationRootChildNode("playlists");
    ImmutableNode rootNode = configuration.getNodeModel().getRootNode().removeChild(playlistsNode);
    if (playlistsNode != null) {
      for (ImmutableNode playlistChildNode : playlistsNode.getChildren()) {
        playlistsNode = playlistsNode.removeChild(playlistChildNode);
      }
    } else {
      LOG.warn("Playlists block not found (save operation)!");
      playlistsNode = new ImmutableNode.Builder().name("playlists").create();
    }

    for (PlaylistData playlistData : playlistsData) {
      ImmutableNode.Builder playlistNodeBuilder = new ImmutableNode.Builder();
      playlistNodeBuilder.name("playlist")
          .addAttribute("name", playlistData.getName());

      playlistData.getTracks().forEach(trackData -> {
        ImmutableNode trackNode = new ImmutableNode.Builder()
            .name("track")
            .addAttribute("sourceType", trackData.getSourceType().name())
            .value(trackData.getTrackPath()).create();
        playlistNodeBuilder.addChild(trackNode);
      });
      playlistsNode = playlistsNode.addChild(playlistNodeBuilder.create());
    }
    rootNode = rootNode.addChild(playlistsNode);
    configuration.getNodeModel().setRootNode(rootNode);
  }

  public void saveAllPlaylists(List<PlaylistData> playlistsData, PlaylistData activePlaylist,
                               PlaylistData displayedPlaylist) throws IllegalArgumentException {
    if ((playlistsData == null) || (activePlaylist == null) || (displayedPlaylist == null)) {
      throw new IllegalArgumentException("Playlists is invalid");
    }

    setPlaylistsInConfiguration(playlistsData);
    configuration.setProperty(ACTIVE_PLAYLIST_NAME_NODE, activePlaylist.getName());
    configuration.setProperty(DISPLAYED_PLAYLIST_NAME_NODE, displayedPlaylist.getName());
    saveConfiguration();
  }

  @Override
  public void saveLastFmUserData(String username, String password) throws IllegalArgumentException {
    if ((username == null) || (password == null)) {
      throw new IllegalArgumentException("User data is invalid");
    }

    configuration.setProperty(LASTFM_USERNAME_NODE, username);
    configuration.setProperty(LASTFM_PASSWORD_NODE, password);
    saveConfiguration();
  }

  private void saveConfiguration() {
    try {
      LOG.debug("save configuration");
      configurationBuilder.save();
    } catch (ConfigurationException e) {
      LOG.error("Error when save configuration to file.", e);
    }
  }
}
