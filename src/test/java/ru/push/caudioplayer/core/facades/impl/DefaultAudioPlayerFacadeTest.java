package ru.push.caudioplayer.core.facades.impl;

import org.apache.commons.collections.CollectionUtils;
import org.testng.annotations.*;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.components.impl.DefaultCustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.impl.DefaultCustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.helpers.MediaInfoDataLoader;
import ru.push.caudioplayer.core.mediaplayer.model.PlaylistModel;
import ru.push.caudioplayer.core.mediaplayer.services.AppConfigurationService;
import ru.push.caudioplayer.core.mediaplayer.services.impl.CommonsAppConfigurationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 3/18/17
 */
public class DefaultAudioPlayerFacadeTest {
  private static final String[] MEDIA_PLAYER_FACTORY_OPTIONS = {
      "--quiet"
  };
  private static final String TEST_CONFIGURATION_FILE_PATH = "test-mediaplayer-app-configuration.xml";

  private AudioPlayerFacade audioPlayerFacade;

  private static final int PLAYLISTS_COUNT = 2;
  private static final String PLAYLIST_FIRST_NAME = "first";
  private static PlaylistModel playlistFirst;
  private static final String PLAYLIST_SECOND_NAME = "second";
  private static PlaylistModel playlistSecond;

  @BeforeClass
  public static void setUpClass() {
//    MediaInfoModel playlistFirstTrack0 = new MediaInfoModel.Builder()
//        .artist("The Beatles")
//        .album("Only A Northern Song")
//        .title("Yellow Submarine")
//        .build();
//    List<MediaInfoModel> playlistFirstTracks = Lists.newArrayList(
//
//    );
//
//    playlistFirst = new PlaylistModel();
  }


  @BeforeMethod
  public void setUp() throws Exception {
    CustomMediaPlayerFactory playerFactory = new CustomMediaPlayerFactory(MEDIA_PLAYER_FACTORY_OPTIONS);
    CustomAudioPlayerComponent playerComponent = new DefaultCustomAudioPlayerComponent(playerFactory);
    CustomPlaylistComponent playlistComponent = new DefaultCustomPlaylistComponent(playerFactory);
    MediaInfoDataLoader mediaInfoDataLoader = new MediaInfoDataLoader(playerFactory);
    AppConfigurationService appConfigurationService = new CommonsAppConfigurationService(TEST_CONFIGURATION_FILE_PATH);

    DefaultAudioPlayerFacade defaultAudioPlayerFacade = new DefaultAudioPlayerFacade();
    defaultAudioPlayerFacade.setPlayerComponent(playerComponent);
    defaultAudioPlayerFacade.setPlaylistComponent(playlistComponent);
    defaultAudioPlayerFacade.setMediaInfoDataLoader(mediaInfoDataLoader);
    defaultAudioPlayerFacade.setAppConfigurationService(appConfigurationService);
    audioPlayerFacade = defaultAudioPlayerFacade;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    audioPlayerFacade.stopApplication();
  }

  @Test
  public void shouldGetPlaylistsFromAppConfig() {
    List<PlaylistModel> playlists = audioPlayerFacade.getPlaylists();

    assertTrue(CollectionUtils.isNotEmpty(playlists),
        "Playlists collection null or empty.");
    assertEquals(playlists.size(), PLAYLISTS_COUNT,
        "Unexpected count of playlists.");

    Map<Integer, List<PlaylistModel>> playlistsGroupedByPosition = playlists.stream()
        .collect(Collectors.groupingBy(PlaylistModel::getPosition));
    assertEquals(playlistsGroupedByPosition.entrySet().size(), playlists.size(),
        "Each playlist must have unique position value.");

    PlaylistModel
  }

}