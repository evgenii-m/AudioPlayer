package ru.push.caudioplayer.core.services;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.core.services.impl.CommonsAppConfigurationService;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * This test class used only for calling service methods for writing in configuration file,
 * but the results are checked manually.
 *
 * @author push <mez.e.s@yandex.ru>
 * @date 5/9/17
 */
@Test
public class AppConfigurationServiceManuallyTest {

  private static final String CONFIGURATION_FILE_NAME = "AppConfigurationServiceManuallyTestResult.xml";
  private static final String PLAYLIST_1_NAME = "playlist1";
  private static final String PLAYLIST_2_NAME = "playlist2";
  private static final String PLAYLIST_3_NAME = "playlist3";

  private final AppConfigurationService appConfigurationService;

  private PlaylistData displayedPlaylist;
  private PlaylistData activePlaylist;
  private List<PlaylistData> playlists;

  AppConfigurationServiceManuallyTest() {
    appConfigurationService = new CommonsAppConfigurationService(CONFIGURATION_FILE_NAME);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    PlaylistData playlist1 = new PlaylistData();
    playlist1.setName(PLAYLIST_1_NAME);
    playlist1.setTracks(
        Arrays.asList(
            new MediaInfoData(PLAYLIST_1_NAME + "_1", MediaSourceType.FILE),
            new MediaInfoData(PLAYLIST_1_NAME + "_2", MediaSourceType.FILE),
            new MediaInfoData(PLAYLIST_1_NAME + "_3", MediaSourceType.FILE)
        )
    );
    PlaylistData playlist2 = new PlaylistData();
    playlist2.setName(PLAYLIST_2_NAME);
    playlist2.setTracks(
        Arrays.asList(
            new MediaInfoData(PLAYLIST_2_NAME + "_1", MediaSourceType.FILE),
            new MediaInfoData(PLAYLIST_2_NAME + "_2", MediaSourceType.FILE),
            new MediaInfoData(PLAYLIST_2_NAME + "_3", MediaSourceType.FILE)
        )
    );
    PlaylistData playlist3 = new PlaylistData();
    playlist3.setName(PLAYLIST_3_NAME);
    playlist3.setTracks(
        Arrays.asList(
            new MediaInfoData(PLAYLIST_3_NAME + "_1", MediaSourceType.HTTP_STREAM),
            new MediaInfoData(PLAYLIST_3_NAME + "_2", MediaSourceType.HTTP_STREAM),
            new MediaInfoData(PLAYLIST_3_NAME + "_3", MediaSourceType.HTTP_STREAM)
        )
    );
    playlists = Arrays.asList(playlist1, playlist2, playlist3);
    activePlaylist = playlist1;
    displayedPlaylist = playlist3;
  }

  @Test
  public void testSaveActivePlaylist() {
    appConfigurationService.saveActivePlaylist(activePlaylist.getName());
    assertTrue(true);
  }

  @Test
  public void testSaveDisplayedPlaylist() {
    appConfigurationService.saveDisplayedPlaylist(displayedPlaylist.getName());
    assertTrue(true);
  }

  @Test
  public void testSavePlaylists() {
    appConfigurationService.savePlaylists(playlists);
    assertTrue(true);
  }

  @Test
  public void testSaveAllPlaylists() {
    appConfigurationService.savePlaylists(playlists, activePlaylist.getName(), displayedPlaylist.getName());
    assertTrue(true);
  }
}
