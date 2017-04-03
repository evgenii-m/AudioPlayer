package ru.push.caudioplayer.core.facades.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.services.AppConfigurationService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 3/18/17
 */
@Test
@ContextConfiguration(locations = {
    "classpath:spring/test-application-context.xml"
})
public class AudioPlayerFacadeIntegrationTest extends AbstractTestNGSpringContextTests {
  @Autowired
  private AudioPlayerFacade audioPlayerFacade;
  @Autowired
  private CustomPlaylistComponent playlistComponent;
  @Autowired
  private AppConfigurationService appConfigurationService;

  private static final int PLAYLISTS_COUNT = 2;
  private static final String FIRST_PLAYLIST_NAME = "first";
  private static final String SECOND_PLAYLIST_NAME = "second";

  @BeforeClass
  public static void setUpClass() {
  }

  @BeforeMethod
  public void setUp() throws Exception {
    // refresh playlist component after each test
    playlistComponent.loadPlaylists(appConfigurationService.getPlaylists());
  }

  @AfterMethod
  public void tearDown() throws Exception {
    audioPlayerFacade.stopApplication();
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#getPlaylists()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#getActivePlaylist()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#getPlaylist(java.lang.String)
   */
  @Test
  public void shouldGetPlaylistsFromAppConfig() {
    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();

    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");
    assertEquals(playlists.size(), PLAYLISTS_COUNT, "Unexpected count of playlists.");

    Set<Integer> positionsSet = playlists.stream()
        .map(PlaylistData::getPosition)
        .collect(Collectors.toSet());
    assertEquals(positionsSet.size(), playlists.size(), "Each playlist must have unique position value.");

    PlaylistData firstPlaylist = audioPlayerFacade.getPlaylist(FIRST_PLAYLIST_NAME);
    assertNotNull(firstPlaylist, "Playlist with name '" + FIRST_PLAYLIST_NAME + "' not found.");
    assertEquals(firstPlaylist.getName(), FIRST_PLAYLIST_NAME, "Unexpected playlist name.");
    assertTrue(CollectionUtils.isNotEmpty(firstPlaylist.getTracks()),
        "Tracks of playlist '" + FIRST_PLAYLIST_NAME + "' null or empty.");

    PlaylistData secondPlaylist = audioPlayerFacade.getPlaylist(SECOND_PLAYLIST_NAME);
    assertNotNull(secondPlaylist, "Playlist with name '" + SECOND_PLAYLIST_NAME + "' not found.");
    assertEquals(secondPlaylist.getName(), SECOND_PLAYLIST_NAME, "Unexpected playlist name.");
    assertTrue(CollectionUtils.isNotEmpty(secondPlaylist.getTracks()),
        "Tracks of playlist '" + SECOND_PLAYLIST_NAME + "' null or empty.");

    PlaylistData activePlaylist = audioPlayerFacade.getActivePlaylist();
    assertNotNull(activePlaylist, "Active playlist must be specified.");
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#showPlaylist(java.lang.String)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#renamePlaylist(java.lang.String, java.lang.String)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#addFilesToPlaylist(java.util.List)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#deleteItemsFromPlaylist(java.util.List)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#addLocationsToPlaylist(java.util.List)
   */
//  @Test
  public void shouldDisplayAndModifyPlaylists() {
//    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();
//    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");

//    PlaylistData inactivePlaylist = IterableUtils.find(
//        playlists, playlist -> !playlist.isActive()
//    );
//    assertNotNull(inactivePlaylist, "Inactive playlist not found.");

//    audioPlayerFacade.addFilesToPlaylist();
  }

}