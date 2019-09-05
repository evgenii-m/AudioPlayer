package ru.push.caudioplayer.core.config.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import ru.push.caudioplayer.core.config.domain.Configuration;
import ru.push.caudioplayer.core.config.domain.DeezerSessionData;
import ru.push.caudioplayer.core.config.domain.LastfmSessionData;
import ru.push.caudioplayer.core.config.domain.PlaylistItem;
import ru.push.caudioplayer.core.config.domain.Playlists;
import ru.push.caudioplayer.core.config.domain.view.Column;
import ru.push.caudioplayer.core.config.domain.view.Columns;
import ru.push.caudioplayer.core.config.domain.view.PlaylistContainer;
import ru.push.caudioplayer.core.config.domain.view.View;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.config.dto.PlaylistContainerViewConfigurations;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/23/17
 */
public class CommonsApplicationConfigService implements ApplicationConfigService {

  private static final Logger LOG = LoggerFactory.getLogger(CommonsApplicationConfigService.class);

  private static final String DEFAULT_CONFIG_FILE_NAME = "mediaplayer-app-configuration.xml";

  private Configuration config;
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

  @Override
  public String getActivePlaylistUid() {
    return config.getPlaylists().getActiveUid();
  }

  @Override
  public String getDisplayedPlaylistUid() {
    return config.getPlaylists().getDisplayedUid();
  }

	@Override
	public List<String> getLocalPlaylistsUid() {
		return config.getPlaylists().getPlaylists().stream()
				.sorted((o1, o2) -> Long.compare(o1.getPosition(), o2.getPosition()))
				.map(PlaylistItem::getPlaylistUid)
				.collect(Collectors.toList());
	}

  @Override
  public void saveActivePlaylist(String playlistUid) {
    Assert.notNull(playlistUid);
    config.getPlaylists().setActiveUid(playlistUid);
    saveConfiguration();
  }

  @Override
  public void saveDisplayedPlaylist(String playlistUid) {
    Assert.notNull(playlistUid);
    config.getPlaylists().setDisplayedUid(playlistUid);
    saveConfiguration();
  }

  @Override
  public void appendPlaylist(String playlistUid) {
		List<String> playlistItemsUid = config.getPlaylists().getPlaylists().stream()
				.map(PlaylistItem::getPlaylistUid)
				.collect(Collectors.toList());

		// added new playlist item
		if (CollectionUtils.isEmpty(playlistItemsUid) || !playlistItemsUid.contains(playlistUid)) {
			config.getPlaylists().getPlaylists().add(new PlaylistItem(playlistUid, playlistItemsUid.size()));
			saveConfiguration();
		}
  }

  @Override
  public void removePlaylist(String playlistUid) {
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
