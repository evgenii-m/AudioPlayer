package ru.push.caudioplayer.core.config.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import ru.push.caudioplayer.core.config.domain.Configuration;
import ru.push.caudioplayer.core.config.domain.DeezerSessionData;
import ru.push.caudioplayer.core.config.domain.LastfmSessionData;
import ru.push.caudioplayer.core.config.domain.Playlist;
import ru.push.caudioplayer.core.config.domain.Playlists;
import ru.push.caudioplayer.core.config.domain.SourceType;
import ru.push.caudioplayer.core.config.domain.Track;
import ru.push.caudioplayer.core.config.domain.view.Column;
import ru.push.caudioplayer.core.config.domain.view.Columns;
import ru.push.caudioplayer.core.config.domain.view.PlaylistContainer;
import ru.push.caudioplayer.core.config.domain.view.View;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.core.services.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.facades.domain.configuration.PlaylistContainerViewConfigurations;
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
  private static final String UNTITLED_PLAYLIST_NAME = "Untitled";

  @Autowired
  private MediaInfoDataLoaderService mediaInfoDataLoaderService;

  private Configuration configuration;
  private final String configurationPath;


  public CommonsApplicationConfigService(String configurationFileName) {
  	configurationPath = configurationFileName;

		try {
			String configurationContent = new String(Files.readAllBytes(Paths.get(configurationFileName)), StandardCharsets.UTF_8);
			configuration = XmlUtils.unmarshalDocumnet(configurationContent, Configuration.class.getPackage().getName());
			LOG.debug("Configuration loaded: {}", configuration);
		} catch (IOException | JAXBException e) {
			LOG.error("Exception occurred when configuration load, will be created default configuration file.", e);
			configuration = createDefaultConfiguration();
			saveConfiguration();
		}
  }

  public CommonsApplicationConfigService() {
    this(DEFAULT_CONFIG_FILE_NAME);
  }

  private Configuration createDefaultConfiguration() {
  	Configuration defaultConfiguration = new Configuration();
  	defaultConfiguration.setPlaylists(new Playlists());

  	View viewConfig = new View(new PlaylistContainer(new Columns(
  			Arrays.asList(
  					new Column("number", "#", 120),
						new Column("artist", "Artist", 140),
						new Column("album", "Album", 160),
						new Column("title", "Title", 245),
						new Column("Length", "Length", 77)
				)
		)));
  	defaultConfiguration.setView(viewConfig);
  	return defaultConfiguration;
  }

  private void saveConfiguration() {
    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
    		Paths.get(configurationPath), StandardCharsets.UTF_8))
		{
			XmlUtils.marshalDocument(configuration, bufferedWriter, Configuration.class.getPackage().getName());
			LOG.debug("save configuration");
    } catch (JAXBException | IOException e) {
      LOG.error("Error when save configuration to file.", e);
		}
	}


  @Override
  public String getActivePlaylistUid() {
    return configuration.getPlaylists().getActiveUid();
  }

  @Override
  public String getDisplayedPlaylistUid() {
    return configuration.getPlaylists().getDisplayedUid();
  }

  @Override
  public List<PlaylistData> getPlaylists() {

		return configuration.getPlaylists().getPlaylists().stream()
				.map(p -> new PlaylistData(p.getUid(), p.getName(), createMediaInfoDataList(p.getTracks())))
				.collect(Collectors.toList());
  }

  @Override
  public void saveActivePlaylist(PlaylistData activePlaylist) {
    Assert.notNull(activePlaylist);
    configuration.getPlaylists().setActiveUid(activePlaylist.getUid());
    saveConfiguration();
  }

  @Override
  public void saveDisplayedPlaylist(PlaylistData displayedPlaylist) {
    Assert.notNull(displayedPlaylist);
    configuration.getPlaylists().setDisplayedUid(displayedPlaylist.getUid());
    saveConfiguration();
  }


	private List<AudioTrackData> createMediaInfoDataList(List<Track> playlistTracks) {
  	assert playlistTracks != null;

		return playlistTracks.stream()
				.map(p -> mediaInfoDataLoaderService.load(p.getTrackPath(), MediaSourceType.valueOf(p.getSourceType().value())))
				.collect(Collectors.toList());
	}

  @Override
  public void savePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

		List<Playlist> playlists = configuration.getPlaylists().getPlaylists();

    long playlistPosition = 0;

    if (CollectionUtils.isNotEmpty(playlists)) {

			Optional<Playlist> existingPlaylist = playlists.stream()
					.filter(p -> p.getUid().equals(playlistData.getUid()))
					.findFirst();
			if (existingPlaylist.isPresent()) {
				playlistPosition = existingPlaylist.get().getPosition();
				configuration.getPlaylists().getPlaylists().remove(existingPlaylist.get());
			} else {
				playlistPosition = playlists.size();
			}
    }

    playlists.add(convertPlaylist(playlistData, playlistPosition));

    saveConfiguration();
  }

  @Override
  public void renamePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

		Optional<Playlist> renamedPlaylist = configuration.getPlaylists().getPlaylists().stream()
				.filter(p -> p.getUid().equals(playlistData.getUid()))
				.findFirst();
		if (renamedPlaylist.isPresent()) {
			renamedPlaylist.get().setName(playlistData.getName());
			saveConfiguration();
		} else {
			LOG.warn("Playlist with UID '" + playlistData.getUid() + "' not found in set, renaming aborted.");
		}
  }

  @Override
  public void deletePlaylist(PlaylistData playlistData) {
    Assert.notNull(playlistData);

    configuration.getPlaylists().getPlaylists().removeIf(playlist -> playlist.getUid().equals(playlistData.getUid()));

    // refresh position attribute for playlist nodes to prevent gaps
		List<Playlist> playlists = configuration.getPlaylists().getPlaylists();
		Map<Integer, Long> nodeIdxPositionMap = playlists.stream()
				.collect(Collectors.toMap(
						playlists::indexOf,
						Playlist::getPosition,
						(e1, e2) -> e1
				));
		final List<Integer> nodeIdxList = nodeIdxPositionMap.entrySet().stream()
				.sorted((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		for (int i = 0; i < nodeIdxList.size(); i++) {
			playlists.get(nodeIdxList.get(i)).setPosition(i);
		}

    saveConfiguration();
  }

  @Override
  public void saveAllPlaylists(List<PlaylistData> playlistsData, PlaylistData activePlaylist,
                               PlaylistData displayedPlaylist) {
    Assert.notNull(playlistsData);
    Assert.notNull(activePlaylist);
    Assert.notNull(displayedPlaylist);

		Playlists playlistsConfig = configuration.getPlaylists();

		playlistsConfig.setActiveUid(activePlaylist.getUid());
		playlistsConfig.setDisplayedUid(displayedPlaylist.getUid());

		List<Playlist> playlists = playlistsData.stream()
				.map(o -> convertPlaylist(o, playlistsData.indexOf(o)))
				.collect(Collectors.toList());
		playlistsConfig.setPlaylists(playlists);

    saveConfiguration();
  }

  private Playlist convertPlaylist(PlaylistData data, long position) {
		return new Playlist(data.getUid(), data.getName(), position,
				data.getTracks().stream()
						.map(t -> new Track(SourceType.fromValue(t.getSourceType().name()), t.getTrackPath()))
						.collect(Collectors.toList())
		);
	}

  @Override
  public void saveLastFmSessionData(LastFmSessionData sessionData) {
    Assert.notNull(sessionData);
    Assert.notNull(sessionData.getUsername());
		Assert.notNull(sessionData.getSessionKey());

		if (configuration.getLastfmSessionData() == null) {
			configuration.setLastfmSessionData(new LastfmSessionData());
		}

		configuration.getLastfmSessionData().setUsername(sessionData.getUsername());
    // TODO: add hashing for session key
		configuration.getLastfmSessionData().setSessionKey(sessionData.getSessionKey());

    saveConfiguration();
  }

	@Override
	public LastFmSessionData getLastFmSessionData() {
		Optional<LastfmSessionData> sessionDataOptional = Optional.ofNullable(configuration.getLastfmSessionData());
		String lastfmUsername =  sessionDataOptional.map(LastfmSessionData::getUsername).orElse(null);
		String lastfmSessionKey = sessionDataOptional.map(LastfmSessionData::getSessionKey).orElse(null);
		return ((lastfmUsername != null) && (lastfmSessionKey != null)) ?
				new LastFmSessionData(lastfmUsername, lastfmSessionKey) :
				null;
	}

	@Override
	public void saveDeezerAccessToken(String accessToken) {
  	Assert.notNull(accessToken);

		if (configuration.getDeezerSessionData() == null) {
			configuration.setDeezerSessionData(new DeezerSessionData());
		}

		// TODO: add hashing for access token
  	configuration.getDeezerSessionData().setAccessToken(accessToken);

  	saveConfiguration();
	}

	@Override
	public String getDeezerAccessToken() {
		return Optional.ofNullable(configuration.getDeezerSessionData())
				.map(DeezerSessionData::getAccessToken).orElse(null);
	}

	@Override
  public PlaylistContainerViewConfigurations getPlaylistContainerViewConfigurations() throws ConfigurationException {
		List<Column> columnsConfig = configuration.getView().getPlaylistContainer().getColumns().getColumns();

		if (columnsConfig == null) {
      throw new ConfigurationException("Invalid playlist container view configuration.");
    }

		List<PlaylistContainerViewConfigurations.PlaylistContainerColumn> playlistContainerColumns = columnsConfig.stream()
				.map(column -> new PlaylistContainerViewConfigurations.PlaylistContainerColumn(
						column.getName(), column.getTitle(), column.getWidth()))
				.collect(Collectors.toList());
    return new PlaylistContainerViewConfigurations(playlistContainerColumns);
  }

  @Override
  public void savePlaylistContainerViewConfigurations(PlaylistContainerViewConfigurations viewConfigurations) {
    Assert.notNull(viewConfigurations);

		List<Column> columns = viewConfigurations.getColumns().stream()
				.map(column -> new Column(column.getName(), column.getTitle(), column.getWidth()))
				.collect(Collectors.toList());
    configuration.getView().getPlaylistContainer().getColumns().setColumns(columns);

    saveConfiguration();
  }

}
