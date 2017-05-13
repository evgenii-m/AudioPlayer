package ru.push.caudioplayer.core.services.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.core.services.AppConfigurationService;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    } catch (ConfigurationException ex) {
      LOG.error("Exception occurred when configuration load, will be created default configuration file.", ex);
      createDefaultConfiguration();
    }
  }

  public CommonsAppConfigurationService() {
    this(DEFAULT_CONFIG_FILE_NAME);
  }

  private void createDefaultConfiguration() {
    configuration = new XMLConfiguration();
    configuration.getNodeModel().setRootNode(
        new ImmutableNode.Builder().name("configuration").create()
    );
    saveConfiguration();
  }

  private ImmutableNode getConfigurationRootChildNode(String nodeName) {
    return configuration.getNodeModel().getRootNode().getChildren().stream()
        .filter(node -> node.getNodeName().equals(nodeName)).findFirst()
        .orElse(null);
  }

  private ImmutableNode getRootNode() {
    return configuration.getNodeModel().getRootNode();
  }

  @Override
  public String getActivePlaylistName() {
    ImmutableNode activePlaylistNode = getConfigurationRootChildNode("activePlaylist");
    if (activePlaylistNode != null) {
      return (String) activePlaylistNode.getValue();
    } else {
      LOG.warn("Active playlist not specified in configuration file!");
      return null;
    }
  }

  @Override
  public String getDisplayedPlaylistName() {
    ImmutableNode displayedPlaylistNode = getConfigurationRootChildNode("displayedPlaylist");
    if (displayedPlaylistNode != null) {
      return (String) displayedPlaylistNode.getValue();
    } else {
      LOG.warn("Displayed playlist not specified in configuration file!");
      return null;
    }
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    ImmutableNode playlistsNode = getConfigurationRootChildNode("playlists");

    if (playlistsNode != null && CollectionUtils.isNotEmpty(playlistsNode.getChildren())) {
      List<PlaylistData> playlists = playlistsNode.getChildren().stream()
          .map(playlistNode -> {
            String playlistName = (playlistNode.getAttributes().get("name") != null) ?
                (String) playlistNode.getAttributes().get("name") : UNTITLED_PLAYLIST_NAME;
            List<MediaInfoData> playlistTracks = playlistNode.getChildren().stream()
                .map(trackNode -> {
                  String trackPath = (String) trackNode.getValue();
                  MediaSourceType sourceType = MediaSourceType.valueOf(
                      StringUtils.upperCase((String) trackNode.getAttributes().getOrDefault("sourceType", "FILE"))
                  );
                  return mediaInfoDataLoaderService.load(trackPath, sourceType);
                }).collect(Collectors.toList());
            return new PlaylistData(playlistName, playlistTracks);
          }).collect(Collectors.toList());
      return playlists;

    } else {
      LOG.warn("Playlists block not found or empty (load operation)!");
      PlaylistData emptyPlaylist = new PlaylistData();
      return Collections.singletonList(emptyPlaylist);
    }
  }

  @Override
  public void saveActivePlaylist(@NotNull String activePlaylistName) {
    setActivePlaylistInConfiguration(activePlaylistName);
    saveConfiguration();
  }

  private void setActivePlaylistInConfiguration(@NotNull String activePlaylistName) {
    ImmutableNode activePlaylistNode = getConfigurationRootChildNode("activePlaylist");
    ImmutableNode rootNode = (activePlaylistNode != null) ?
        getRootNode().replaceChild(
            activePlaylistNode, activePlaylistNode.setValue(activePlaylistName)
        ) :
        getRootNode().addChild(
            new ImmutableNode.Builder()
                .name("activePlaylist")
                .value(activePlaylistName)
                .create()
        );
    configuration.getNodeModel().setRootNode(rootNode);
  }

  @Override
  public void saveDisplayedPlaylist(@NotNull String displayedPlaylistName) {
    setDisplayedPlaylistInConfiguration(displayedPlaylistName);
    saveConfiguration();
  }

  private void setDisplayedPlaylistInConfiguration(@NotNull String displayedPlaylistName) {
    ImmutableNode displayedPlaylistNode = getConfigurationRootChildNode("displayedPlaylist");
    ImmutableNode rootNode = (displayedPlaylistNode != null) ?
        getRootNode().replaceChild(
            displayedPlaylistNode, displayedPlaylistNode.setValue(displayedPlaylistName)
        ) :
        getRootNode().addChild(
            new ImmutableNode.Builder()
                .name("displayedPlaylist")
                .value(displayedPlaylistName)
                .create()
        );
    configuration.getNodeModel().setRootNode(rootNode);
  }

  @Override
  public void savePlaylists(@NotNull List<PlaylistData> playlistsData) {
    setPlaylistsInConfiguration(playlistsData);
    saveConfiguration();
  }

  private void setPlaylistsInConfiguration(@NotNull List<PlaylistData> playlistsData) {
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

  // TODO: make function for change individual playlist
  public void savePlaylists(@NotNull List<PlaylistData> playlistsData, @NotNull String activePlaylistName,
                            @NotNull String displayedPlaylistName)
  {
    setPlaylistsInConfiguration(playlistsData);
    setActivePlaylistInConfiguration(activePlaylistName);
    setDisplayedPlaylistInConfiguration(displayedPlaylistName);
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
