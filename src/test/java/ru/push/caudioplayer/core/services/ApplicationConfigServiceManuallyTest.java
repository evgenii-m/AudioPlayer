package ru.push.caudioplayer.core.services;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.config.impl.CommonsApplicationConfigService;
import ru.push.caudioplayer.core.facades.domain.configuration.PlaylistContainerViewConfigurations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * This test class used only for calling service methods to write in configuration result file or read
 * sample configuration file, but the results are checked manually.
 *
 * @author push <mez.e.s@yandex.ru>
 * @date 5/9/17
 */
@Test
public class ApplicationConfigServiceManuallyTest {

  private static final String RESULT_CONFIGURATION_FILE_NAME = "AppConfigurationServiceManuallyTestResult.xml";
  private static final String SAMPLE_CONFIGURATION_FILE_NAME = "test-mediaplayer-app-configuration.xml";

  private static final String PLAYLIST_1_NAME = "playlist1";
  private static final String PLAYLIST_2_NAME = "playlist2";
  private static final String PLAYLIST_3_NAME = "playlist3";
  private static final String LASTFM_USERNAME = "testUsername";
  private static final String LASTFM_PASSWORD = "testPassword";

  private static ApplicationConfigService applicationConfigServiceForWrite;
  private static ApplicationConfigService applicationConfigServiceForRead;

  private static PlaylistData displayedPlaylist;
  private static PlaylistData activePlaylist;
  private static List<PlaylistData> playlists;

  @BeforeClass
  public static void setUpClass() throws Exception {
    PlaylistData playlist1 = new PlaylistData();
    playlist1.setName(PLAYLIST_1_NAME);
    playlist1.setTracks(
        Arrays.asList(
            new AudioTrackData(PLAYLIST_1_NAME + "_1", MediaSourceType.FILE),
            new AudioTrackData(PLAYLIST_1_NAME + "_2", MediaSourceType.FILE),
            new AudioTrackData(PLAYLIST_1_NAME + "_3", MediaSourceType.FILE)
        )
    );
    PlaylistData playlist2 = new PlaylistData();
    playlist2.setName(PLAYLIST_2_NAME);
    playlist2.setTracks(
        Arrays.asList(
            new AudioTrackData(PLAYLIST_2_NAME + "_1", MediaSourceType.FILE),
            new AudioTrackData(PLAYLIST_2_NAME + "_2", MediaSourceType.FILE),
            new AudioTrackData(PLAYLIST_2_NAME + "_3", MediaSourceType.FILE)
        )
    );
    PlaylistData playlist3 = new PlaylistData();
    playlist3.setName(PLAYLIST_3_NAME);
    playlist3.setTracks(
        Arrays.asList(
            new AudioTrackData(PLAYLIST_3_NAME + "_1", MediaSourceType.HTTP_STREAM),
            new AudioTrackData(PLAYLIST_3_NAME + "_2", MediaSourceType.HTTP_STREAM),
            new AudioTrackData(PLAYLIST_3_NAME + "_3", MediaSourceType.HTTP_STREAM)
        )
    );
    playlists = Arrays.asList(playlist1, playlist2, playlist3);
    activePlaylist = playlist1;
    displayedPlaylist = playlist3;

    try {
      Files.delete(Paths.get(RESULT_CONFIGURATION_FILE_NAME));
    } catch (NoSuchFileException e) {
      // correct situation - continue
    } catch (IOException e) {
      fail("Exception when delete test configuration file", e);
    }

    applicationConfigServiceForWrite = new CommonsApplicationConfigService(RESULT_CONFIGURATION_FILE_NAME);
    applicationConfigServiceForRead = new CommonsApplicationConfigService(SAMPLE_CONFIGURATION_FILE_NAME);
  }

  @Test
  public void testSaveActivePlaylist() {
    applicationConfigServiceForWrite.saveActivePlaylist(activePlaylist);
    assertTrue(true);
  }

  @Test
  public void testSaveDisplayedPlaylist() {
    applicationConfigServiceForWrite.saveDisplayedPlaylist(displayedPlaylist);
    assertTrue(true);
  }

  @Test
  public void testAddPlaylist() {
    applicationConfigServiceForWrite.savePlaylist(playlists.get(0));
    assertTrue(true);
  }

  @Test
  public void testUpdatePlaylist() {
    applicationConfigServiceForWrite.savePlaylist(playlists.get(0));
    applicationConfigServiceForWrite.savePlaylist(playlists.get(1));
    playlists.get(0).setTracks(playlists.get(2).getTracks());
    applicationConfigServiceForWrite.savePlaylist(playlists.get(0));
    assertTrue(true);
  }

  @Test
  public void testRenamePlaylist() {
    applicationConfigServiceForWrite.savePlaylist(activePlaylist);
    applicationConfigServiceForWrite.savePlaylist(displayedPlaylist);
    activePlaylist.setName("new playlist name");
    applicationConfigServiceForWrite.renamePlaylist(activePlaylist);
    assertTrue(true);
  }

  @Test
  public void testDeletePlaylist() {
    applicationConfigServiceForWrite.savePlaylist(playlists.get(0));
    applicationConfigServiceForWrite.savePlaylist(playlists.get(1));
    applicationConfigServiceForWrite.savePlaylist(playlists.get(2));
    applicationConfigServiceForWrite.deletePlaylist(playlists.get(1));
    assertTrue(true);
  }

  @Test
  public void testSaveAllPlaylists() {
    applicationConfigServiceForWrite.saveAllPlaylists(playlists, activePlaylist, displayedPlaylist);
    assertTrue(true);
  }

  @Test
  public void testGetPlaylistContainerViewConfigurations() throws ConfigurationException {
    PlaylistContainerViewConfigurations playlistContainerViewConfigurations =
        applicationConfigServiceForRead.getPlaylistContainerViewConfigurations();
    assertNotNull(playlistContainerViewConfigurations);
  }

  @Test
  public void testSavePlaylistContainerViewConfigurations() throws ConfigurationException {
    PlaylistContainerViewConfigurations playlistContainerViewConfigurations =
        applicationConfigServiceForRead.getPlaylistContainerViewConfigurations();
    applicationConfigServiceForWrite.savePlaylistContainerViewConfigurations(playlistContainerViewConfigurations);
    assertTrue(true);
  }
}
