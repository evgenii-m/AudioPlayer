package ru.push.caudioplayer.core.facades.impl;

import org.apache.commons.collections.CollectionUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.config.ApplicationConfigService;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * TODO: tests not actual, need modification
 *
 * @author push <mez.e.s@yandex.ru>
 * @date 3/18/17
 */
@Test
@ContextConfiguration(locations = {
    "classpath:spring/test-application-context.xml"
})
public class BussinesLogicFacadesIntegrationTest extends AbstractTestNGSpringContextTests {
  @Autowired
  private AudioPlayerFacade audioPlayerFacade;
	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;
  @Autowired
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
  private ApplicationConfigService applicationConfigService;

  private AudioPlayerEventListener eventListener;

  private static final int PLAYLISTS_COUNT = 2;
  private static final String FIRST_PLAYLIST_UID = "ec1ed359-5344-4b5b-8262-a12c26c609c8";
  private static final String SECOND_PLAYLIST_UID = "b5c65f36-7a1c-4a22-9042-5ca05ff84abd";
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
    reset(applicationConfigService);
    reset(playerComponent);

    eventListener = Mockito.spy(new TestAudioPlayerEventAdapter());
    audioPlayerFacade.addEventListener(eventListener);
		musicLibraryLogicFacade.reloadPlaylists(); // refresh playlist component after each test

    doNothing().when(applicationConfigService).saveActivePlaylist(anyString());
    doNothing().when(applicationConfigService).saveDisplayedPlaylist(anyString());
    doNothing().when(applicationConfigService).appendPlaylist(anyString(), anyString());
		doNothing().when(applicationConfigService).removePlaylist(anyString());
    doNothing().when(applicationConfigService).saveLastFmSessionData(any());
		doNothing().when(applicationConfigService).saveDeezerAccessToken(anyString());
    doReturn(Boolean.TRUE).when(playerComponent).playMedia(anyString());
  }

  @AfterMethod
  public void tearDown() throws Exception {
    audioPlayerFacade.releaseAudioPlayer();
    audioPlayerFacade.removeEventListener(eventListener);
  }

  private class TestAudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#getPlaylists()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#getActivePlaylist()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#getDisplayedPlaylist()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#getPlaylist(java.lang.String)
   */
  @Test
  public void shouldGetPlaylistsFromAppConfig() {
    List<PlaylistData> playlists = musicLibraryLogicFacade.getLocalPlaylists();

    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");
    assertEquals(playlists.size(), PLAYLISTS_COUNT, "Unexpected count of playlists.");

    PlaylistData firstPlaylist = playlists.stream()
				.filter(p -> p.getUid().equals(FIRST_PLAYLIST_UID))
				.findFirst().orElse(null);
    assertNotNull(firstPlaylist, "Playlist with uid '" + FIRST_PLAYLIST_UID + "' not found.");
    assertEquals(firstPlaylist.getTitle(), FIRST_PLAYLIST_NAME, "Unexpected playlist name.");
    assertTrue(CollectionUtils.isNotEmpty(firstPlaylist.getTracks()),
        "Tracks of playlist [" + FIRST_PLAYLIST_UID + "] null or empty.");

    PlaylistData secondPlaylist = playlists.stream()
				.filter(p -> p.getUid().equals(SECOND_PLAYLIST_UID))
				.findFirst().orElse(null);
    assertNotNull(secondPlaylist, "Playlist with uid '" + SECOND_PLAYLIST_UID + "' not found.");
    assertEquals(secondPlaylist.getTitle(), SECOND_PLAYLIST_NAME, "Unexpected playlist name.");
    assertTrue(CollectionUtils.isNotEmpty(secondPlaylist.getTracks()),
        "Tracks of playlist [" + SECOND_PLAYLIST_UID + "] null or empty.");

    PlaylistData activePlaylist = musicLibraryLogicFacade.getActivePlaylist();
    assertNotNull(activePlaylist, "Active playlist must be specified.");
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#createNewPlaylist()
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#renamePlaylist(java.lang.String, java.lang.String)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#deleteByUid(java.lang.String)
   */
  @Test
  public void shouldCreateRenameAndDeletePlaylists() {
//    List<PlaylistData> playlists = musicLibraryLogicFacade.getLocalPlaylists();
//    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");
//
//    int originalPlaylistsSize = playlists.size();
//
//    PlaylistData newPlaylist = musicLibraryLogicFacade.createLocalPlaylist();
//    PlaylistData displayedPlaylist = musicLibraryLogicFacade.getDisplayedPlaylist();
//    assertNotNull(newPlaylist, "New playlist is null.");
//    assertNotNull(displayedPlaylist, "Displayed playlist is null.");
//    assertEquals(newPlaylist, displayedPlaylist, "New playlist must be displayed!");
//    verify(eventListener).createdNewPlaylist(newPlaylist);
//    int actualPlaylistsSize = musicLibraryLogicFacade.getPlaylists().size();
//    assertEquals(actualPlaylistsSize, originalPlaylistsSize + 1, "Expected increase in playlists size.");
//
//    String playlistUid = newPlaylist.getUid();
//    String newPlaylistName = "new playlist name";
//		musicLibraryLogicFacade.renamePlaylist(playlistUid, newPlaylistName);
//    assertEquals(musicLibraryLogicFacade.getPlaylist(playlistUid).getName(), newPlaylistName,
//        "Unexpected playlist [" + playlistUid + "] name.");
//
//    boolean deletePlaylistResult = musicLibraryLogicFacade.deleteByUid(playlistUid);
//    assertTrue(deletePlaylistResult, "Unexpected delete playlist result.");
//    assertNull(musicLibraryLogicFacade.getPlaylist(playlistUid), "Playlist [" + playlistUid + "] must be deleted.");
//    displayedPlaylist = musicLibraryLogicFacade.getDisplayedPlaylist();
//    assertNotNull(displayedPlaylist, "After delete displayed playlist must be changed.");
//    verify(eventListener).changedPlaylist(displayedPlaylist);
//    actualPlaylistsSize = musicLibraryLogicFacade.getPlaylists().size();
//    assertEquals(actualPlaylistsSize, originalPlaylistsSize, "Expected decrease in playlists size.");
//
//    verify(applicationConfigService, times(1)).savePlaylist(any(PlaylistData.class));
//    verify(applicationConfigService, times(1)).renamePlaylist(any(PlaylistData.class));
//    verify(applicationConfigService, times(1)).deleteByUid(any(PlaylistData.class));
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#deleteByUid(java.lang.String)
   */
  @Test
  public void shouldCreateNewPlaylistAfterDeleteLast() {
//    List<PlaylistData> playlists = musicLibraryLogicFacade.getPlaylists();
//    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");
//
//    List<String> playlistsUid = playlists.stream().map(PlaylistData::getUid).collect(Collectors.toList());
//    playlistsUid.stream().forEach(playlistUid -> musicLibraryLogicFacade.deleteByUid(playlistUid));
//
//    playlists = musicLibraryLogicFacade.getPlaylists();
//    assertTrue(CollectionUtils.isNotEmpty(playlists), "New playlist not created after delete last!");
//    assertEquals(playlists.size(), 1, "Only one playlist must be created.");
//    PlaylistData createdPlaylist = playlists.get(0);
//    PlaylistData activePlaylist = musicLibraryLogicFacade.getActivePlaylist();
//    assertEquals(createdPlaylist, activePlaylist, "Created playlist must be active.");
//    PlaylistData displayedPlaylist = musicLibraryLogicFacade.getDisplayedPlaylist();
//    assertEquals(displayedPlaylist, createdPlaylist, "Created playlist must be displayed.");
//
//    verify(applicationConfigService, atLeastOnce()).deleteByUid(any(PlaylistData.class));
//    verify(applicationConfigService, times(1)).savePlaylist(any(PlaylistData.class));
//    verify(eventListener, atLeastOnce()).changedPlaylist(any(PlaylistData.class));
  }

  /**
   * This test affects:
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#addFilesToPlaylist(java.util.List)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#deleteItemsFromPlaylist(java.util.List)
   *  ru.push.caudioplayer.core.facades.AudioPlayerFacade#addLocationsToPlaylist(java.util.List)
   */
  @Test
  public void shouldAddAndDeletePlaylistItems() {
    PlaylistData activePlaylist = musicLibraryLogicFacade.getActivePlaylist();
    assertNotNull(activePlaylist, "Active playlist is null.");

    int tracklistSize = activePlaylist.getTracks().size();
		musicLibraryLogicFacade.addFilesToPlaylist(
				activePlaylist.getUid(),
        Arrays.stream(MEDIA_FILES_PATHS).map(File::new).collect(Collectors.toList())
    );
    int expectedPlaylistSize = tracklistSize + MEDIA_FILES_PATHS.length;
    int actualTracklistSize = activePlaylist.getTracks().size();
    assertEquals(actualTracklistSize, expectedPlaylistSize, "Unexpected tracklist size after add files.");

    tracklistSize = activePlaylist.getTracks().size();
		musicLibraryLogicFacade.addLocationsToPlaylist(activePlaylist.getUid(), Arrays.asList(MEDIA_LOCATIONS_PATHS));
    expectedPlaylistSize = tracklistSize + MEDIA_LOCATIONS_PATHS.length;
    actualTracklistSize = activePlaylist.getTracks().size();
    assertEquals(actualTracklistSize, expectedPlaylistSize, "Unexpected tracklist size after add locations.");

//    tracklistSize = activePlaylist.getTracks().size();
//    List<Integer> deletedItemIndexes = Arrays.asList(0, 1);
//		musicLibraryLogicFacade.deleteItemsFromPlaylist(activePlaylist.getUid(), deletedItemIndexes);
//    expectedPlaylistSize = tracklistSize - deletedItemIndexes.size();
//    actualTracklistSize = activePlaylist.getTracks().size();
//    assertEquals(actualTracklistSize, expectedPlaylistSize, "Unexpected tracklist size after delete items.");

    verify(eventListener, times(3)).changedPlaylist(activePlaylist);
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
//    List<PlaylistData> playlists = musicLibraryLogicFacade.getPlaylists();
//    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");
//
//    // tests must consider that active and displayed playlist may be different
//    PlaylistData activePlaylist = musicLibraryLogicFacade.getActivePlaylist();
//    PlaylistData inactivePlaylist = IterableUtils.find(
//        playlists, playlistData -> !playlistData.equals(activePlaylist)
//    );
//    assertNotNull(inactivePlaylist, "There must be at least one inactive playlist.");
//    assertNotEquals(inactivePlaylist, activePlaylist, "Active and inactive playlist must be different!");
//    PlaylistData displayedPlaylist = musicLibraryLogicFacade.showPlaylist(inactivePlaylist.getUid());
//    assertEquals(displayedPlaylist, inactivePlaylist, "Inactive playlist must be displayed.");
//
//    // for this test, the tracks in playlist must be different and size of each playlist must be at least 2!
//    assertTrue((activePlaylist.getTracks().size() >= 2), "Playlist size must be at least 2!");
//    assertTrue((displayedPlaylist.getTracks().size() >= 2), "Playlist size must be at least 2!");
//    AudioTrackData currentTrackInfo = audioPlayerFacade.getActivePlaylistTrack();
//    assertNotNull(currentTrackInfo, "Facade must provide the current track info even before play media.");
//
//    audioPlayerFacade.playCurrentTrack();
//    currentTrackInfo = audioPlayerFacade.getActivePlaylistTrack();
//    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());
//
//    AudioTrackData prevTrackInfo = audioPlayerFacade.getActivePlaylistTrack();
//    audioPlayerFacade.playNextTrack();
//    currentTrackInfo = audioPlayerFacade.getActivePlaylistTrack();
//    assertNotEquals(prevTrackInfo, currentTrackInfo, "Media info data must be changed!");
//    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());
//
//    audioPlayerFacade.playPrevTrack();
//    currentTrackInfo = audioPlayerFacade.getActivePlaylistTrack();
//    assertEquals(currentTrackInfo, prevTrackInfo, "Media info data must be changed back to previous!");
//    verify(playerComponent, times(2)).playMedia(currentTrackInfo.getTrackPath());
//
//    // TODO: add checks for track position when verify changedTrackPosition methods
//    verify(eventListener, times(3)).changedTrackPosition(eq(activePlaylist), anyInt());
//
//    audioPlayerFacade.playTrack(displayedPlaylist.getUid(), 0);
//    currentTrackInfo = audioPlayerFacade.getActivePlaylistTrack();
//    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());
//
//    audioPlayerFacade.playNextTrack();
//    final AudioTrackData trackInfo = audioPlayerFacade.getActivePlaylistTrack();
//    boolean trackInDisplayedPlaylist = displayedPlaylist.getTracks().stream()
//        .anyMatch(track -> track.equals(trackInfo));
//    assertTrue(trackInDisplayedPlaylist,
//        "When playlist changed switching tracks must be applied for actual playlist!");
//    verify(playerComponent).playMedia(currentTrackInfo.getTrackPath());
//
//    // TODO: add checks for track position when verify changedTrackPosition methods
//    verify(eventListener, times(2)).changedTrackPosition(eq(displayedPlaylist), anyInt());
  }
}