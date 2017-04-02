package ru.push.caudioplayer.core.facades.impl;

import org.apache.commons.collections.CollectionUtils;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;

import java.util.List;
import java.util.Map;
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

  private static final int PLAYLISTS_COUNT = 2;
  private static final String PLAYLIST_FIRST_NAME = "first";
  private static PlaylistData playlistFirst;
  private static final String PLAYLIST_SECOND_NAME = "second";
  private static PlaylistData playlistSecond;

  @BeforeClass
  public static void setUpClass() {
//    MediaInfoData playlistFirstTrack0 = new MediaInfoData.Builder()
//        .artist("The Beatles")
//        .album("Only A Northern Song")
//        .title("Yellow Submarine")
//        .build();
//    List<MediaInfoData> playlistFirstTracks = Lists.newArrayList(
//
//    );
//
//    playlistFirst = new PlaylistData();
  }


  @BeforeMethod
  public void setUp() throws Exception {
  }

  @AfterMethod
  public void tearDown() throws Exception {
    audioPlayerFacade.stopApplication();
  }

  @Test
  public void shouldGetPlaylistsFromAppConfig() {
    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();

    assertTrue(CollectionUtils.isNotEmpty(playlists), "Playlists collection null or empty.");
    assertEquals(playlists.size(), PLAYLISTS_COUNT, "Unexpected count of playlists.");

    Set<Integer> positionsSet = playlists.stream()
        .map(PlaylistData::getPosition)
        .collect(Collectors.toSet());
    assertEquals(positionsSet.size(), playlists.size(), "Each playlist must have unique position value.");

    PlaylistData firstPlaylist = audioPlayerFacade.getPlaylist(PLAYLIST_FIRST_NAME);
    assertNotNull(firstPlaylist, "Playlist with name '" + PLAYLIST_FIRST_NAME + "' not found.");
    assertEquals(firstPlaylist.getName(), PLAYLIST_FIRST_NAME, "Unexpected playlist name.");

    PlaylistData activePlaylist = audioPlayerFacade.getActivePlaylist();
    assertNotNull(activePlaylist, "Active playlist must be specified.");
  }

  @Test
  public void shouldLoadAudioTracksInfo() {
    PlaylistData firstPlaylist = audioPlayerFacade.getPlaylist(PLAYLIST_FIRST_NAME);

    List<MediaInfoData> tracks = firstPlaylist.getTracks();
    assertTrue(CollectionUtils.isNotEmpty(tracks), "Playlist tracks collection null or empty.");

//    tracks.forEach(mediaInfoData -> );

  }

}