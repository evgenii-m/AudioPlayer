package ru.push.caudioplayer.core.mediaplayer.services.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.services.AppConfigurationService;

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
  private static final String DEFAULT_PLAYLIST_NAME = "Untitled";

  private final XMLConfiguration configuration;

  public CommonsAppConfigurationService(String configurationFileName) {
    Configurations conf = new Configurations();
    try {
      configuration = conf.xml(configurationFileName);
    } catch (ConfigurationException ex) {
      throw new IllegalStateException("Application configuration load failed.", ex);
    }
  }

  public CommonsAppConfigurationService() {
    this(DEFAULT_CONFIG_FILE_NAME);
  }

  @Override
  public List<PlaylistData> getPlaylists() {
    List<String> playlistNames = configuration.getList(String.class, "playlists.playlist[@name]");

    if (CollectionUtils.isNotEmpty(playlistNames)) {
      return playlistNames.stream()
          .map(playlistName -> {
            int playlistIndex = playlistNames.indexOf(playlistName);
            int playlistPosition = configuration.getInt("playlists.playlist(" + playlistIndex + ")[@position]");
            boolean playlistActive =
                (configuration.getString("playlists.playlist(" + playlistIndex + ")[@active]") != null);
            // when read playlists from configuration.xml, put to PlaylistData.tracks DTO with
            // single filled field 'trackPath', others fields will be loaded if required
            List<MediaInfoData> playlistTracks =
                configuration.getList(String.class, "playlists.playlist(" + playlistIndex + ").track").stream()
                    .map(MediaInfoData::new)
                    .collect(Collectors.toList());
            return new PlaylistData(playlistName, playlistPosition, playlistTracks, playlistActive);
          }).collect(Collectors.toList());
    } else {
      PlaylistData emptyPlaylist = new PlaylistData(DEFAULT_PLAYLIST_NAME, 0, true);
      return Collections.singletonList(emptyPlaylist);
    }
  }
}
