package ru.push.caudioplayer.core.mediaplayer.services.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.services.AppConfigurationService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public class CommonsAppConfigurationService implements AppConfigurationService {
  private static final Logger LOG = LoggerFactory.getLogger(CommonsAppConfigurationService.class);
  private static final String DEFAULT_CONFIG_FILE_NAME = "mediaplayer-app-configuration.xml";
  private static final String UNTITLED_PLAYLIST_NAME = "Untitled";

  private final FileBasedConfigurationBuilder<XMLConfiguration> configurationBuilder;
  private XMLConfiguration configuration;

  public CommonsAppConfigurationService(String configurationFileName) {
    Parameters params = new Parameters();
    configurationBuilder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class)
        .configure(params.xml().setFileName(configurationFileName));
    try {
      configuration = configurationBuilder.getConfiguration();
    } catch (ConfigurationException ex) {
      throw new IllegalStateException("Application configuration load failed.", ex);
    }
  }

  public CommonsAppConfigurationService() {
    this(DEFAULT_CONFIG_FILE_NAME);
  }

  private ImmutableNode getConfigurationRootChildNode(String nodeName) {
    return configuration.getNodeModel().getRootNode().getChildren().stream()
        .filter(node -> node.getNodeName().equals(nodeName))
        .findFirst().orElse(null);
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    ImmutableNode playlistsNode = getConfigurationRootChildNode("playlists");

    if (playlistsNode != null && CollectionUtils.isNotEmpty(playlistsNode.getChildren())) {
      List<PlaylistData> playlists = playlistsNode.getChildren().stream()
          .map(playlistNode -> {
            int playlistIndex = playlistNode.getChildren().indexOf(playlistNode);
            int playlistPosition = (playlistNode.getAttributes().get("position") != null) ?
                Integer.valueOf((String) playlistNode.getAttributes().get("position")) : playlistIndex;
            String playlistName = (playlistNode.getAttributes().get("name") != null) ?
                (String) playlistNode.getAttributes().get("name") : UNTITLED_PLAYLIST_NAME;
            boolean playlistActive = Optional.ofNullable(playlistNode.getAttributes().get("active")).isPresent();
            // when read playlists from configuration.xml, put to PlaylistData.tracks DTO with
            // single filled field 'trackPath', others fields will be loaded if required
            List<MediaInfoData> playlistTracks =
                playlistNode.getChildren().stream()
                    .map(trackNode -> (String) trackNode.getValue())
                    .map(MediaInfoData::new)
                    .collect(Collectors.toList());
            return new PlaylistData(playlistName, playlistPosition, playlistTracks, playlistActive);
          }).collect(Collectors.toList());
      if (playlists.stream().noneMatch(PlaylistData::isActive)) {
        playlists.stream()
            .findFirst()
            .ifPresent(firstPlaylist -> firstPlaylist.setActive(true));
      }
      return playlists;
    } else {
      LOG.warn("Playlists block not found or empty (load operation)!");
      PlaylistData emptyPlaylist = new PlaylistData(0);
      return Collections.singletonList(emptyPlaylist);
    }
  }

  public void savePlaylists(List<PlaylistData> playlistsData) {
    ImmutableNode playlistsNode = getConfigurationRootChildNode("playlists");
    ImmutableNode rootNode = configuration.getNodeModel().getRootNode().removeChild(playlistsNode);
    if (playlistsNode != null) {
      for (ImmutableNode playlistChildNode : playlistsNode.getChildren()) {
        playlistsNode = playlistsNode.removeChild(playlistChildNode);
      }
    } else {
      LOG.warn("Playlists block not found (save operation)!");
      playlistsNode = new ImmutableNode.Builder().name("playlists").create();
      configuration.getNodeModel().getRootNode().addChild(playlistsNode);
    }

    for (PlaylistData playlistData : playlistsData) {
      ImmutableNode.Builder playlistNodeBuilder = new ImmutableNode.Builder();
      playlistNodeBuilder.name("playlist")
          .addAttribute("name", playlistData.getName())
          .addAttribute("position", playlistData.getPosition());
      if (playlistData.isActive()) {
        playlistNodeBuilder.addAttribute("active", true);
      }

      playlistData.getTracks().forEach(trackData -> {
        ImmutableNode trackNode = new ImmutableNode.Builder()
            .name("track")
            .value(trackData.getTrackPath()).create();
        playlistNodeBuilder.addChild(trackNode);
      });
      playlistsNode = playlistsNode.addChild(playlistNodeBuilder.create());
    }
    rootNode = rootNode.addChild(playlistsNode);
    configuration.getNodeModel().setRootNode(rootNode);
    saveConfiguration();
  }

  private void saveConfiguration() {
    try {
      configurationBuilder.save();
    } catch (ConfigurationException e) {
      LOG.error("Error when save configuration to file.", e);
    }
  }
}
