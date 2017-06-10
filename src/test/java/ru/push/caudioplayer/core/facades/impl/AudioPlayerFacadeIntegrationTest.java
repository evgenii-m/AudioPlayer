package ru.push.caudioplayer.core.facades.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.core.services.AppConfigurationService;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
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
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
  private AppConfigurationService appConfigurationService;

  private AudioPlayerEventListener eventListener;

  private static final int PLAYLISTS_COUNT = 2;
  private static final String FIRST_PLAYLIST_NAME = "first";
  private static final String SECOND_PLAYLIST_NAME = "second";
  private static final String[] MEDIA_FILES_PATHS = {
      "audiotracks/01 Yellow Submarine.mp3",
      "audiotracks/02 Hey Bulldog.mp3"
  };
  private static final String[] MEDIA_LOCATIONS_PATHS = {
      "http://ice1.somafm.com/groovesalad-128.mp3",
      "http://ice1.somafm.com/secretagent-128.mp3"
  };

  @BeforeClass
  public static void setUpClass() {
  }

  @BeforeMethod
  public void setUp() throws Exception {
    reset(appConfigurationService);
    reset(playerComponent);

    eventListener = Mockito.spy(new TestAudioPlayerEventAdapter());
    audioPlayerFacade.addEventListener(eventListener);
    audioPlayerFacade.refreshPlaylists(); // refresh playlist component after each test

    doNothing().when(appConfigurationService).saveActivePlaylist(any(PlaylistData.class));
    doNothing().when(appConfigurationService).saveDisplayedPlaylist(any(PlaylistData.class));
    doNothing().when(appConfigurationService).savePlaylist(any(PlaylistData.class));
    doNothing().when(appConfigurationService).saveAllPlaylists(anyListOf(PlaylistData.class),
        any(PlaylistData.class), any(PlaylistData.class));
    doReturn(Boolean.TRUE).when(playerComponent).playMedia(anyString());
  }

  @AfterMethod
  public void tearDown() throws Exception {
    audioPlayerFacade.stopApplication();
    verify(eventListener).stopAudioPlayer();
    audioPlayerFacade.removeEventListener(eventListener);
  }

  private class TestAudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {
    @Override
    public void stopAudioPlayer() {
    }
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
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#createNewPlaylist()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#renamePlaylist(java.lang.String, java.lang.String)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#deletePlaylist(java.lang.String)
   */
  @Test
  public void shouldCreateRenameAndDeletePlaylists() {
    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();
    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");

    int originalPlaylistsSize = playlists.size();

    PlaylistData newPlaylist = audioPlayerFacade.createNewPlaylist();
    PlaylistData displayedPlaylist = audioPlayerFacade.getDisplayedPlaylist();
    assertNotNull(newPlaylist, "New playlist is null.");
    assertNotNull(displayedPlaylist, "Displayed playlist is null.");
    assertEquals(newPlaylist, displayedPlaylist, "New playlist must be displayed!");
    verify(eventListener).createdNewPlaylist(newPlaylist);
    int actualPlaylistsSize = audioPlayerFacade.getPlaylists().size();
    assertEquals(actualPlaylistsSize, originalPlaylistsSize + 1, "Expected increase in playlists size.");

    String actualPlaylistName = newPlaylist.getName();
    String newPlaylistName = "new playlist name";
    audioPlayerFacade.renamePlaylist(actualPlaylistName, newPlaylistName);
    assertNotNull(audioPlayerFacade.getPlaylist(newPlaylistName), "Playlist with name '"
        + newPlaylistName + "' not found.");

    boolean deletePlaylistResult = audioPlayerFacade.deletePlaylist(newPlaylistName);
    assertTrue(deletePlaylistResult, "Unexpected delete playlist result.");
    assertNull(audioPlayerFacade.getPlaylist(newPlaylistName),
        "Playlist with name '" + newPlaylistName + "' must be deleted.");
    displayedPlaylist = audioPlayerFacade.getDisplayedPlaylist();
    assertNotNull(displayedPlaylist, "After delete displayed playlist must be changed.");
    verify(eventListener).changedPlaylist(displayedPlaylist);
    actualPlaylistsSize = audioPlayerFacade.getPlaylists().size();
    assertEquals(actualPlaylistsSize, originalPlaylistsSize, "Expected decrease in playlists size.");

    verify(appConfigurationService, times(2)).savePlaylist(any(PlaylistData.class));
    verify(appConfigurationService, times(1)).saveAllPlaylists(anyListOf(PlaylistData.class),
        any(PlaylistData.class), any(PlaylistData.class));
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#deletePlaylist(java.lang.String)
   */
  @Test
  public void shouldCreateNewPlaylistAfterDeleteLast() {
    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();
    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");

    Set<String> playlistsNames = playlists.stream().map(PlaylistData::getName).collect(Collectors.toSet());
    playlistsNames.forEach(playlistName -> audioPlayerFacade.deletePlaylist(playlistName));

    playlists = audioPlayerFacade.getPlaylists();
    assertTrue(CollectionUtils.isNotEmpty(playlists), "New playlist not created after delete last!");
    assertEquals(playlists.size(), 1, "Only one playlist must be created.");
    PlaylistData createdPlaylist = playlists.get(0);
    PlaylistData activePlaylist = audioPlayerFacade.getActivePlaylist();
    assertEquals(createdPlaylist, activePlaylist, "Created playlist must be active.");
    PlaylistData displayedPlaylist = audioPlayerFacade.getDisplayedPlaylist();
    assertEquals(displayedPlaylist, createdPlaylist, "Created playlist must be displayed.");

    verify(appConfigurationService, atLeastOnce()).saveAllPlaylists(anyListOf(PlaylistData.class),
        any(PlaylistData.class), any(PlaylistData.class));
    verify(eventListener, atLeastOnce()).changedPlaylist(any(PlaylistData.class));
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#addFilesToPlaylist(java.util.List)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#deleteItemsFromPlaylist(java.util.List)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#addLocationsToPlaylist(java.util.List)
   */
  @Test
  public void shouldAddAndDeletePlaylistItems() {
    PlaylistData displayedPlaylist = audioPlayerFacade.showActivePlaylist();
    assertNotNull(displayedPlaylist, "Displayed playlist is null.");

    int tracklistSize = displayedPlaylist.getTracks().size();
    audioPlayerFacade.addFilesToPlaylist(Arrays.asList(MEDIA_FILES_PATHS).stream()
        .map(File::new).collect(Collectors.toList()));
    int expectedPlaylistSize = tracklistSize + MEDIA_FILES_PATHS.length;
    int actualTracklistSize = displayedPlaylist.getTracks().size();
    assertEquals(actualTracklistSize, expectedPlaylistSize, "Unexpected tracklist size after add files.");

    tracklistSize = displayedPlaylist.getTracks().size();
    audioPlayerFacade.addLocationsToPlaylist(Arrays.asList(MEDIA_LOCATIONS_PATHS));
    expectedPlaylistSize = tracklistSize + MEDIA_LOCATIONS_PATHS.length;
    actualTracklistSize = displayedPlaylist.getTracks().size();
    assertEquals(actualTracklistSize, expectedPlaylistSize, "Unexpected tracklist size after add locations.");

    tracklistSize = displayedPlaylist.getTracks().size();
    List<Integer> deletedItemIndexes = Arrays.asList(0, 1);
    audioPlayerFacade.deleteItemsFromPlaylist(deletedItemIndexes);
    expectedPlaylistSize = tracklistSize - deletedItemIndexes.size();
    actualTracklistSize = displayedPlaylist.getTracks().size();
    assertEquals(actualTracklistSize, expectedPlaylistSize, "Unexpected tracklist size after delete items.");

    verify(appConfigurationService, times(3)).savePlaylist(any(PlaylistData.class));
    verify(eventListener, times(3)).changedPlaylist(displayedPlaylist);
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#playTrack(java.lang.String, int)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#playCurrentTrack()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#playNextTrack()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#playPrevTrack()
   */
  @Test
  public void shouldPlayAndSwitchPlaylistTracks() {
    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();
    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");

    // tests must consider that active and displayed playlist may be different
    PlaylistData activePlaylist = audioPlayerFacade.getActivePlaylist();
    PlaylistData inactivePlaylist = IterableUtils.find(
        playlists, playlistData -> !playlistData.equals(activePlaylist)
    );
    assertNotNull(inactivePlaylist, "There must be at least one inactive playlist.");
    assertNotEquals(inactivePlaylist, activePlaylist, "Active and inactive playlist must be different!");
    PlaylistData displayedPlaylist = audioPlayerFacade.showPlaylist(inactivePlaylist.getName());
    assertEquals(displayedPlaylist, inactivePlaylist, "Inactive playlist must be displayed.");

    // for this test, the tracks in playlist must be different and size of each playlist must be at least 2!
    assertTrue((activePlaylist.getTracks().size() >= 2), "Playlist size must be at least 2!");
    assertTrue((displayedPlaylist.getTracks().size() >= 2), "Playlist size must be at least 2!");
    MediaInfoData currentTrackInfo = audioPlayerFacade.getCurrentTrackInfo();
    assertNotNull(currentTrackInfo, "Facade must provide the current track info even before play media.");

    audioPlayerFacade.playCurrentTrack();
    currentTrackInfo = audioPlayerFacade.getCurrentTrackInfo();
    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());

    MediaInfoData prevTrackInfo = audioPlayerFacade.getCurrentTrackInfo();
    audioPlayerFacade.playNextTrack();
    currentTrackInfo = audioPlayerFacade.getCurrentTrackInfo();
    assertNotEquals(prevTrackInfo, currentTrackInfo, "Media info data must be changed!");
    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());

    audioPlayerFacade.playPrevTrack();
    currentTrackInfo = audioPlayerFacade.getCurrentTrackInfo();
    assertEquals(currentTrackInfo, prevTrackInfo, "Media info data must be changed back to previous!");
    verify(playerComponent, times(2)).playMedia(currentTrackInfo.getTrackPath());

    // TODO: add checks for track position when verify changedTrackPosition methods
    verify(eventListener, times(3)).changedTrackPosition(eq(activePlaylist.getName()), anyInt());

    audioPlayerFacade.playTrack(displayedPlaylist.getName(), 0);
    currentTrackInfo = audioPlayerFacade.getCurrentTrackInfo();
    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());

    audioPlayerFacade.playNextTrack();
    final MediaInfoData trackInfo = audioPlayerFacade.getCurrentTrackInfo();
    boolean trackInDisplayedPlaylist = displayedPlaylist.getTracks().stream()
        .anyMatch(track -> track.equals(trackInfo));
    assertTrue(trackInDisplayedPlaylist,
        "When playlist changed switching tracks must be applied for actual playlist!");
    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());

    // TODO: add checks for track position when verify changedTrackPosition methods
    verify(eventListener, times(2)).changedTrackPosition(eq(displayedPlaylist.getName()), anyInt());
  }
}