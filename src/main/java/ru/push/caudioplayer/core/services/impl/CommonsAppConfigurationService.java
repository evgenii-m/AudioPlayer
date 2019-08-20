package ru.push.caudioplayer.core.services.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.services.AppConfigurationService;
import ru.push.caudioplayer.ui.configuration.PlaylistContainerViewConfigurations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.push.caudioplayer.core.services.ConfigurationServiceConstants.*;

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

  private String getNodeAttribute(ImmutableNode node, String attributeName) {
    assert node != null;
    assert StringUtils.isNoneEmpty(attributeName);

    return (String) node.getAttributes().get(attributeName);
  }

  private String getNodeAttribute(ImmutableNode node, String attributeName, String defaultValue) {
    assert node != null;
    assert StringUtils.isNoneEmpty(attributeName);
    assert defaultValue != null;

    return (String) node.getAttributes().getOrDefault(attributeName, defaultValue);
  }

  private void saveConfiguration() {
    try {
      LOG.debug("save configuration");
      configurationBuilder.save();
    } catch (ConfigurationException e) {
      LOG.error("Error when save configuration to file.", e);
    }
  }


  @Override
  public String getActivePlaylistUid() {
    return configuration.getString(ACTIVE_PLAYLIST_UID_NODE);
  }

  @Override
  public String getDisplayedPlaylistUid() {
    return configuration.getString(DISPLAYED_PLAYLIST_UID_NODE);
  }

  private List<AudioTrackData> createMediaInfoDataList(List<ImmutableNode> playlistTrackNodes) {
    assert playlistTrackNodes != null;

    return playlistTrackNodes.stream()
        .map(trackNode -> {
          String trackPath = (String) trackNode.getValue();
          String sourceTypeName = StringUtils.upperCase(
              getNodeAttribute(trackNode, PLAYLIST_TRACK_NODE_ATTR_SOURCE_TYPE, MediaSourceType.FILE.name())
          );
          MediaSourceType sourceType = MediaSourceType.valueOf(sourceTypeName);
          return mediaInfoDataLoaderService.load(trackPath, sourceType);
        }).collect(Collectors.toList());
  }

  private PlaylistData createPlaylistData(ImmutableNode playlistNode) {
    assert playlistNode != null;

    String playlistUid = getNodeAttribute(playlistNode, PLAYLIST_NODE_ATTR_UID);
    if (StringUtils.isEmpty(playlistUid)) {
      LOG.error("Detected playlist with empty UID, new UID for this playlist will be generated.");
      // generating will be made at playlist object creation
    }

    String playlistName = getNodeAttribute(playlistNode, PLAYLIST_NODE_ATTR_NAME);
    if (StringUtils.isEmpty(playlistName)) {
      LOG.error("Detected playlist with empty name, default name for this playlist will be set.");
      playlistName = UNTITLED_PLAYLIST_NAME;
    }

    List<AudioTrackData> playlistTracks = createMediaInfoDataList(playlistNode.getChildren());

    return (playlistUid != null) ?
        new PlaylistData(playlistUid, playlistName, playlistTracks) :
        new PlaylistData(playlistName, playlistTracks);
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    ImmutableNode playlistsNode = getConfigurationRootChildNode(PLAYLISTS_SET_NODE);

    if (playlistsNode != null && CollectionUtils.isNotEmpty(playlistsNode.getChildren())) {
      return playlistsNode.getChildren().stream()
          .filter(node -> PLAYLIST_NODE_NAME.equals(node.getNodeName()))
          .sorted((node1, node2) -> {
            int node1Position = Integer.valueOf((String) node1.getAttributes()
                .getOrDefault(PLAYLIST_NODE_ATTR_POSITION, 0));
            int node2Position = Integer.valueOf((String) node2.getAttributes()
                .getOrDefault(PLAYLIST_NODE_ATTR_POSITION, 0));
            return Integer.compare(node1Position, node2Position);
          })
          .map(this::createPlaylistData)
          .collect(Collectors.toList());

    } else {
      LOG.warn("Playlists block not found or empty (load operation)!");
      return null;
    }
  }

  @Override
  public void saveActivePlaylist(PlaylistData activePlaylist) {
    Assert.notNull(activePlaylist);
    configuration.setProperty(ACTIVE_PLAYLIST_UID_NODE, activePlaylist.getUid());
    saveConfiguration();
  }

  @Override
  public void saveDisplayedPlaylist(PlaylistData displayedPlaylist) {
    Assert.notNull(displayedPlaylist);
    configuration.setProperty(DISPLAYED_PLAYLIST_UID_NODE, displayedPlaylist.getUid());
    saveConfiguration();
  }

  private List<ImmutableNode> createPlaylistTrackNodes(List<AudioTrackData> tracks) {
    assert tracks != null;

    return tracks.stream()
        .map(trackData ->
            new ImmutableNode.Builder()
                .name(PLAYLIST_TRACK_NODE_NAME)
                .addAttribute(PLAYLIST_TRACK_NODE_ATTR_SOURCE_TYPE, trackData.getSourceType().name())
                .value(trackData.getTrackPath())
                .create()
        ).collect(Collectors.toList());
  }

  private ImmutableNode createPlaylistNode(PlaylistData playlistData, Integer playlistPosition) {
    assert playlistData != null;
    assert playlistPosition >= 0;

    return new ImmutableNode.Builder()
        .name(PLAYLIST_NODE_NAME)
        .addAttribute(PLAYLIST_NODE_ATTR_UID, playlistData.getUid())
        .addAttribute(PLAYLIST_NODE_ATTR_NAME, playlistData.getName())
        .addAttribute(PLAYLIST_NODE_ATTR_POSITION, playlistPosition.toString())
        .addChildren(createPlaylistTrackNodes(playlistData.getTracks()))
        .create();
  }

  @Override
  public void savePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

    ImmutableNode playlistsNode = getConfigurationRootChildNode(PLAYLISTS_SET_NODE);
    int playlistPosition = 0;

    if ((playlistsNode != null) && CollectionUtils.isNotEmpty(playlistsNode.getChildren())) {
      ImmutableNode playlistNode = IterableUtils.find(
          playlistsNode.getChildren(),
          node -> playlistData.getUid().equals(getNodeAttribute(node, PLAYLIST_NODE_ATTR_UID))
      );

      if (playlistNode != null) {
        // changing existing playlist - clear node before add new from playlist data and store position
        playlistPosition = Integer.valueOf((String) playlistNode.getAttributes()
            .getOrDefault(PLAYLIST_NODE_ATTR_POSITION, 0));
        int playlistIndex = playlistsNode.getChildren().indexOf(playlistNode);
        configuration.getNodeModel().clearTree(PLAYLIST_NODE + "(" + playlistIndex + ")", configuration);
      } else {
        // creating new playlist - placed on last position
        playlistPosition = playlistsNode.getChildren().size();
      }
    }

    ImmutableNode newPlaylistNode = createPlaylistNode(playlistData, playlistPosition);
    configuration.addNodes(PLAYLISTS_SET_NODE, Collections.singletonList(newPlaylistNode));
    saveConfiguration();
  }

  @Override
  public void renamePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

    ImmutableNode playlistsNode = getConfigurationRootChildNode(PLAYLISTS_SET_NODE);
    if (playlistsNode == null) {
      LOG.error("Playlists node is null");
      return;
    }

    int playlistIndex = IterableUtils.indexOf(
        playlistsNode.getChildren(),
        node -> playlistData.getUid().equals(getNodeAttribute(node, PLAYLIST_NODE_ATTR_UID))
    );
    if (playlistIndex < 0) {
      LOG.warn("Playlist with UID '" + playlistData.getUid() + "' not found in set, renaming aborted.");
      return;
    }

    configuration.setProperty(PLAYLIST_NODE + "(" + playlistIndex + ")[@" + PLAYLIST_NODE_ATTR_NAME + "]",
        playlistData.getName());
    saveConfiguration();
  }

  private void refreshPlaylistsPositions() {
    LOG.debug("refreshPlaylistsPositions start");

    final ImmutableNode playlistsNode = getConfigurationRootChildNode(PLAYLISTS_SET_NODE);
    Map<Integer, Integer> nodeIdxPositionMap = playlistsNode.getChildren().stream()
        .collect(Collectors.toMap(
            node -> playlistsNode.getChildren().indexOf(node),
            node -> Integer.valueOf(getNodeAttribute(node, PLAYLIST_NODE_ATTR_POSITION)),
            (e1, e2) -> e1
        ));
    final List<Integer> nodeIdxList = nodeIdxPositionMap.entrySet().stream()
        .sorted((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    for (int i = 0; i < nodeIdxList.size(); i++) {
      configuration.setProperty(PLAYLIST_NODE + "(" + nodeIdxList.get(i) + ")[@" + PLAYLIST_NODE_ATTR_POSITION + "]", i);
    }

    LOG.debug("refreshPlaylistsPositions end");
  }

  @Override
  public void deletePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

    ImmutableNode playlistsNode = getConfigurationRootChildNode(PLAYLISTS_SET_NODE);
    if (playlistsNode == null) {
      LOG.error("Playlists node is null");
      return;
    }

    int playlistIndex = IterableUtils.indexOf(
        playlistsNode.getChildren(),
        node -> playlistData.getUid().equals(getNodeAttribute(node, PLAYLIST_NODE_ATTR_UID))
    );
    if (playlistIndex < 0) {
      LOG.warn("Playlist with UID '" + playlistData.getUid() + "' not found in set, renaming aborted.");
      return;
    }

    configuration.clearTree(PLAYLIST_NODE + "(" + playlistIndex + ")");
    refreshPlaylistsPositions();  // refresh position attribute for playlist nodes to prevent gaps
    saveConfiguration();
  }

  @Override
  public void saveAllPlaylists(List<PlaylistData> playlistsData, PlaylistData activePlaylist,
                               PlaylistData displayedPlaylist) {
    Assert.notNull(playlistsData);
    Assert.notNull(activePlaylist);
    Assert.notNull(displayedPlaylist);

    configuration.clearTree(PLAYLISTS_SET_NODE);

    List<ImmutableNode> playlistsNode = playlistsData.stream()
        .map(playlistData -> createPlaylistNode(playlistData, playlistsData.indexOf(playlistData)))
        .collect(Collectors.toList());
    configuration.addNodes(PLAYLISTS_SET_NODE, playlistsNode);

    configuration.setProperty(ACTIVE_PLAYLIST_UID_NODE, activePlaylist.getUid());
    configuration.setProperty(DISPLAYED_PLAYLIST_UID_NODE, displayedPlaylist.getUid());

    saveConfiguration();
  }


  @Override
  public void saveLastFmSessionData(LastFmSessionData sessionData) {
    Assert.notNull(sessionData);
    Assert.notNull(sessionData.getUsername());
		Assert.notNull(sessionData.getSessionKey());

    configuration.setProperty(LASTFM_USERNAME_NODE, sessionData.getUsername());
    // TODO: add hashing for session key
    configuration.setProperty(LASTFM_SESSION_KEY_NODE, sessionData.getSessionKey());

    saveConfiguration();
  }

	@Override
	public LastFmSessionData getLastFmSessionData() {
		String lastfmUsername = configuration.getString(LASTFM_USERNAME_NODE);
		String lastfmSessionKey = configuration.getString(LASTFM_SESSION_KEY_NODE);
		return ((lastfmUsername != null) && (lastfmSessionKey != null)) ?
				new LastFmSessionData(lastfmUsername, lastfmSessionKey) :
				null;
	}

	@Override
	public void saveDeezerAccessToken(String accessToken) {
  	Assert.notNull(accessToken);

		// TODO: add hashing for access token
  	configuration.setProperty(DEEZER_ACCESS_TOKEN_NODE, accessToken);

  	saveConfiguration();
	}

	@Override
	public String getDeezerAccessToken() {
		return configuration.getString(DEEZER_ACCESS_TOKEN_NODE);
	}


	@Override
  public PlaylistContainerViewConfigurations getPlaylistContainerViewConfigurations() throws ConfigurationException {
    // TODO: add handling empty view configuration situations
    HierarchicalConfiguration<ImmutableNode> playlistContainerColumnsConfiguration =
        configuration.configurationAt(PLAYLIST_CONTAINER_COLUMNS_SET_NODE);

    if (playlistContainerColumnsConfiguration == null) {
      throw new ConfigurationException("Invalid playlist container view configuration.");
    }

    List<PlaylistContainerViewConfigurations.PlaylistContainerColumn> playlistContainerColumns =
        playlistContainerColumnsConfiguration.getNodeModel().getInMemoryRepresentation().getChildren().stream()
            .map(node -> {
              // TODO: add checks for column attributes
              String name = getNodeAttribute(node, PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_NAME);
              String title = getNodeAttribute(node, PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_TITLE);
              double width = Double.valueOf(getNodeAttribute(node, PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_WIDTH));
              return new PlaylistContainerViewConfigurations.PlaylistContainerColumn(
                  name, title, width
              );
            })
            .collect(Collectors.toList());

    return new PlaylistContainerViewConfigurations(playlistContainerColumns);
  }

  @Override
  public void savePlaylistContainerViewConfigurations(PlaylistContainerViewConfigurations viewConfigurations) {
    Assert.notNull(viewConfigurations);

    configuration.clearTree(PLAYLIST_CONTAINER_NODE);

    List<ImmutableNode> columnsNodes = viewConfigurations.getColumns().stream()
        .map(column ->
            new ImmutableNode.Builder()
                .name(PLAYLIST_CONTAINER_COLUMN_NODE_NAME)
                .addAttribute(PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_NAME, column.getName())
                .addAttribute(PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_TITLE, column.getTitle())
                .addAttribute(PLAYLIST_CONTAINER_COLUMN_NODE_ATTR_WIDTH, column.getWidth())
                .create()
        ).collect(Collectors.toList());

    configuration.addNodes(PLAYLIST_CONTAINER_COLUMNS_SET_NODE, columnsNodes);

    saveConfiguration();
  }

}
