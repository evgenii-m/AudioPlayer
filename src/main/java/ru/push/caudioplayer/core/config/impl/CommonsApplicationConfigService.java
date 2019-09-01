package ru.push.caudioplayer.core.config.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import ru.push.caudioplayer.core.config.ImportExportConverter;
import ru.push.caudioplayer.core.config.domain.Configuration;
import ru.push.caudioplayer.core.config.domain.DeezerSessionData;
import ru.push.caudioplayer.core.config.domain.LastfmSessionData;
import ru.push.caudioplayer.core.config.domain.PlaylistConfig;
import ru.push.caudioplayer.core.config.domain.PlaylistItem;
import ru.push.caudioplayer.core.config.domain.Playlists;
import ru.push.caudioplayer.core.config.domain.view.Column;
import ru.push.caudioplayer.core.config.domain.view.Columns;
import ru.push.caudioplayer.core.config.domain.view.PlaylistContainer;
import ru.push.caudioplayer.core.config.domain.view.View;
import ru.push.caudioplayer.core.facades.domain.PlaylistType;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.facades.domain.configuration.PlaylistContainerViewConfigurations;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public class CommonsApplicationConfigService implements ApplicationConfigService {

  private static final Logger LOG = LoggerFactory.getLogger(CommonsApplicationConfigService.class);

  private static final String DEFAULT_CONFIG_FILE_NAME = "mediaplayer-app-configuration.xml";
  private static final String DEFAULT_PLAYLISTS_FOLDER_PATH = "playlists/";
  private static final String DEFAULT_PLAYLIST_FILE_EXT = ".xml";
  private static final String UNTITLED_PLAYLIST_NAME = "Untitled";

  @Autowired
	private ImportExportConverter importExportConverter;

  private Configuration config;
  private Map<String, PlaylistConfig> playlistConfigMap;
  private final String configurationPath;


  // TODO: add configuration validation
  public CommonsApplicationConfigService(String configurationFileName) throws IOException {
  	configurationPath = configurationFileName;

		try {
			String configurationContent = new String(Files.readAllBytes(Paths.get(configurationFileName)), StandardCharsets.UTF_8);
			config = XmlUtils.unmarshalDocumnet(configurationContent, Configuration.class.getPackage().getName());
			LOG.debug("Configuration loaded: {}", config);
		} catch (IOException | JAXBException e) {
			LOG.error("Exception occurred when configuration load, will be created default configuration file.", e);
			config = createDefaultConfiguration();
			saveConfiguration();
		}

		loadPlaylists();
  }

  public CommonsApplicationConfigService() throws IOException {
    this(DEFAULT_CONFIG_FILE_NAME);
  }

  private Configuration createDefaultConfiguration() {
  	Configuration defaultConfiguration = new Configuration();
  	defaultConfiguration.setPlaylists(new Playlists());

		PlaylistContainer localPlaylistContainer = new PlaylistContainer(new Columns(
				Arrays.asList(
						new Column("number", "#", 120),
						new Column("artist", "Artist", 140),
						new Column("album", "Album", 160),
						new Column("title", "Title", 245),
						new Column("Length", "Length", 77)
				)
		));
		PlaylistContainer deezerPlaylistContainer = new PlaylistContainer(new Columns(
				Arrays.asList(
						new Column("number", "#", 120),
						new Column("artist", "Artist", 140),
						new Column("album", "Album", 160),
						new Column("title", "Title", 245),
						new Column("Length", "Length", 77)
				)
		));
		View viewConfig = new View(localPlaylistContainer, deezerPlaylistContainer);
  	defaultConfiguration.setView(viewConfig);
  	return defaultConfiguration;
  }

  private void loadPlaylists() throws IOException {

		Path playlistsFolderPath = Paths.get(DEFAULT_PLAYLISTS_FOLDER_PATH);
		if (Files.notExists(playlistsFolderPath) || !Files.isDirectory(playlistsFolderPath)) {
			Files.createDirectories(playlistsFolderPath);
		}
		playlistConfigMap = new HashMap<>();

		List<String> playlistsUid = config.getPlaylists().getPlaylists().stream()
				.map(PlaylistItem::getPlaylistUid)
				.collect(Collectors.toList());
		try (Stream<Path> paths = Files.walk(Paths.get(DEFAULT_PLAYLISTS_FOLDER_PATH))) {
			paths
					.filter(Files::isRegularFile)
					.forEach(p -> {
						LOG.debug("Playlist folder file detected: {}", p);
						try {
							String playlistConfigContent = new String(Files.readAllBytes(p));
							PlaylistConfig playlistConfig = XmlUtils.unmarshalDocumnet(playlistConfigContent, PlaylistConfig.class.getPackage().getName());
							if (playlistsUid.contains(playlistConfig.getUid())) {
								playlistConfigMap.put(playlistConfig.getUid(), playlistConfig);
							}
						} catch (JAXBException | IOException e) {
							LOG.error("Playlist loading error: file path = {}, error = {}", p, e);
						}
					});
		}
	}

  private void saveConfiguration() {
    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
    		Paths.get(configurationPath), StandardCharsets.UTF_8))
		{
			XmlUtils.marshalDocument(config, bufferedWriter, Configuration.class.getPackage().getName());
			LOG.debug("save configuration");
    } catch (JAXBException | IOException e) {
      LOG.error("Error when save configuration to file.", e);
		}
	}

	private void savePlaylistConfig(PlaylistConfig playlistConfig) {
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
				constructPlaylistPath(playlistConfig.getUid()), StandardCharsets.UTF_8))
		{
			XmlUtils.marshalDocument(playlistConfig, bufferedWriter, PlaylistConfig.class.getPackage().getName());
			LOG.debug("save playlist {}", playlistConfig.getUid());
		} catch (JAXBException | IOException e) {
			LOG.error("Error when save playlist configuration to file.", e);
		}
	}

	private Path constructPlaylistPath(String playlistUid) {
		return Paths.get(DEFAULT_PLAYLISTS_FOLDER_PATH + playlistUid + DEFAULT_PLAYLIST_FILE_EXT);
	}


  @Override
  public String getActivePlaylistUid() {
    return config.getPlaylists().getActiveUid();
  }

  @Override
  public String getDisplayedPlaylistUid() {
    return config.getPlaylists().getDisplayedUid();
  }

  @Override
  public List<PlaylistData> getPlaylists() {
  	return config.getPlaylists().getPlaylists().stream()
				.sorted((o1, o2) -> Long.compare(o1.getPosition(), o2.getPosition()))
				.filter(o -> {
					if (!playlistConfigMap.containsKey(o.getPlaylistUid())) {
						LOG.warn("Playlist configuration not found: {}", o.getPlaylistUid());
						return false;
					}
					return true;
				})
				.map(o -> playlistConfigMap.get(o.getPlaylistUid()))
				.map(o -> importExportConverter.convertPlaylist(o))
				.collect(Collectors.toList());
  }

  @Override
  public void saveActivePlaylist(PlaylistData activePlaylist) {
    Assert.notNull(activePlaylist);
    config.getPlaylists().setActiveUid(activePlaylist.getUid());
    saveConfiguration();
  }

  @Override
  public void saveDisplayedPlaylist(PlaylistData displayedPlaylist) {
    Assert.notNull(displayedPlaylist);
    config.getPlaylists().setDisplayedUid(displayedPlaylist.getUid());
    saveConfiguration();
  }

  @Override
  public void savePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

    if (PlaylistType.DEEZER.equals(playlistData.getPlaylistType())) {
    	throw new IllegalStateException("Saving Deezer playlist to configuration not provided");
		}

		PlaylistConfig playlistConfig = importExportConverter.convertPlaylist(playlistData);
		savePlaylistConfig(playlistConfig);
		playlistConfigMap.put(playlistData.getUid(), playlistConfig);

		List<String> playlistItemsUid = config.getPlaylists().getPlaylists().stream()
				.map(PlaylistItem::getPlaylistUid)
				.collect(Collectors.toList());

		// added new playlist item
		if (CollectionUtils.isEmpty(playlistItemsUid) || !playlistItemsUid.contains(playlistData.getUid())) {
			saveConfiguration();
			config.getPlaylists().getPlaylists().add(new PlaylistItem(playlistData.getUid(), playlistItemsUid.size()));
		}
  }

  @Override
  public void renamePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

		PlaylistConfig renamedPlaylist = playlistConfigMap.get(playlistData.getUid());
		if (renamedPlaylist != null) {
			renamedPlaylist.setName(playlistData.getName());
			savePlaylistConfig(renamedPlaylist);
			saveConfiguration();
		} else {
			LOG.warn("Playlist with UID '" + playlistData.getUid() + "' not found in set, renaming aborted.");
		}
  }

  @Override
  public void deletePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

		String playlistUid = playlistData.getUid();
		if (!playlistConfigMap.containsKey(playlistUid)) {
    	LOG.warn("delete playlist not found: {}", playlistUid);
    	return;
		}

		try {
			Files.delete(constructPlaylistPath(playlistUid));
		} catch (IOException e) {
			LOG.error("Delete playlist file error: uid = {}, error = {}", playlistUid, e);
		}
		playlistConfigMap.remove(playlistUid);
    config.getPlaylists().getPlaylists().removeIf(playlist -> playlist.getPlaylistUid().equals(playlistUid));

    // refresh position attribute for playlist nodes to prevent gaps
		List<PlaylistItem> playlistItems = config.getPlaylists().getPlaylists();
		Map<Integer, Long> nodeIdxPositionMap = playlistItems.stream()
				.collect(Collectors.toMap(
						playlistItems::indexOf,
						PlaylistItem::getPosition,
						(e1, e2) -> e1
				));
		final List<Integer> nodeIdxList = nodeIdxPositionMap.entrySet().stream()
				.sorted((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		for (int i = 0; i < nodeIdxList.size(); i++) {
			playlistItems.get(nodeIdxList.get(i)).setPosition(i);
		}
    saveConfiguration();
  }

  @Override
  public void saveLastFmSessionData(LastFmSessionData sessionData) {
    Assert.notNull(sessionData);
    Assert.notNull(sessionData.getUsername());
		Assert.notNull(sessionData.getSessionKey());

		if (config.getLastfmSessionData() == null) {
			config.setLastfmSessionData(new LastfmSessionData());
		}

		config.getLastfmSessionData().setUsername(sessionData.getUsername());
    // TODO: add hashing for session key
		config.getLastfmSessionData().setSessionKey(sessionData.getSessionKey());

    saveConfiguration();
  }

	@Override
	public LastFmSessionData getLastFmSessionData() {
		Optional<LastfmSessionData> sessionDataOptional = Optional.ofNullable(config.getLastfmSessionData());
		String lastfmUsername =  sessionDataOptional.map(LastfmSessionData::getUsername).orElse(null);
		String lastfmSessionKey = sessionDataOptional.map(LastfmSessionData::getSessionKey).orElse(null);
		return ((lastfmUsername != null) && (lastfmSessionKey != null)) ?
				new LastFmSessionData(lastfmUsername, lastfmSessionKey) :
				null;
	}

	@Override
	public void saveDeezerAccessToken(String accessToken) {
  	Assert.notNull(accessToken);

		if (config.getDeezerSessionData() == null) {
			config.setDeezerSessionData(new DeezerSessionData());
		}

		// TODO: add hashing for access token
  	config.getDeezerSessionData().setAccessToken(accessToken);

  	saveConfiguration();
	}

	@Override
	public String getDeezerAccessToken() {
		return Optional.ofNullable(config.getDeezerSessionData())
				.map(DeezerSessionData::getAccessToken).orElse(null);
	}

	@Override
  public PlaylistContainerViewConfigurations getPlaylistContainerViewConfigurations() {
    return new PlaylistContainerViewConfigurations(
				getContainerColumnsViewConfiguration(config.getView().getLocalPlaylistContainer().getColumns())
		);
  }

	@Override
	public PlaylistContainerViewConfigurations getDeezerPlaylistContainerViewConfigurations() {
		return new PlaylistContainerViewConfigurations(
				getContainerColumnsViewConfiguration(config.getView().getDeezerPlaylistContainer().getColumns())
		);
	}

	private List<PlaylistContainerViewConfigurations.PlaylistContainerColumn> getContainerColumnsViewConfiguration(Columns columnsConfig) {
		return columnsConfig.getColumns().stream()
				.map(column -> new PlaylistContainerViewConfigurations.PlaylistContainerColumn(
						column.getName(), column.getTitle(), column.getWidth()))
				.collect(Collectors.toList());
	}

	@Override
  public void savePlaylistContainerViewConfigurations(PlaylistContainerViewConfigurations viewConfigurations) {
    Assert.notNull(viewConfigurations);

		List<Column> columns = viewConfigurations.getColumns().stream()
				.map(column -> new Column(column.getName(), column.getTitle(), column.getWidth()))
				.collect(Collectors.toList());
    config.getView().getLocalPlaylistContainer().getColumns().setColumns(columns);

    saveConfiguration();
  }

}
