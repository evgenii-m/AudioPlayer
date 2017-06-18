package ru.push.caudioplayer.core.services;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.core.services.impl.CommonsAppConfigurationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
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
  private static final String LASTFM_USERNAME = "testUsername";
  private static final String LASTFM_PASSWORD = "testPassword";

  private static AppConfigurationService appConfigurationService;
  private static PlaylistData displayedPlaylist;
  private static PlaylistData activePlaylist;
  private static List<PlaylistData> playlists;

  @BeforeClass
  public static void setUpClass() throws Exception {
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

    try {
      Files.delete(Paths.get(CONFIGURATION_FILE_NAME));
    } catch (NoSuchFileException e) {
      // correct situation - continue
    } catch (IOException e) {
      fail("Exception when delete test configuration file", e);
    }

    appConfigurationService = new CommonsAppConfigurationService(CONFIGURATION_FILE_NAME);
  }

  @Test
  public void testSaveActivePlaylist() {
    appConfigurationService.saveActivePlaylist(activePlaylist);
    assertTrue(true);
  }

  @Test
  public void testSaveDisplayedPlaylist() {
    appConfigurationService.saveDisplayedPlaylist(displayedPlaylist);
    assertTrue(true);
  }

  @Test
  public void testAddPlaylist() {
    appConfigurationService.savePlaylist(playlists.get(0));
    assertTrue(true);
  }

  @Test
  public void testUpdatePlaylist() {
    appConfigurationService.savePlaylist(playlists.get(0));
    appConfigurationService.savePlaylist(playlists.get(1));
    playlists.get(0).setTracks(playlists.get(2).getTracks());
    appConfigurationService.savePlaylist(playlists.get(0));
    assertTrue(true);
  }

  @Test
  public void testRenamePlaylist() {
    appConfigurationService.savePlaylist(activePlaylist);
    appConfigurationService.savePlaylist(displayedPlaylist);
    activePlaylist.setName("new playlist name");
    appConfigurationService.renamePlaylist(activePlaylist);
    assertTrue(true);
  }

  @Test
  public void testDeletePlaylist() {
    appConfigurationService.savePlaylist(playlists.get(0));
    appConfigurationService.savePlaylist(playlists.get(1));
    appConfigurationService.savePlaylist(playlists.get(2));
    appConfigurationService.deletePlaylist(playlists.get(1));
    assertTrue(true);
  }

  @Test
  public void testSaveAllPlaylists() {
    appConfigurationService.saveAllPlaylists(playlists, activePlaylist, displayedPlaylist);
    assertTrue(true);
  }

  @Test
  public void testSaveLastFmUserData() {
    appConfigurationService.saveLastFmUserData(LASTFM_USERNAME, LASTFM_PASSWORD);
    assertTrue(true);
  }
}
