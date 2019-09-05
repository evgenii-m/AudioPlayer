package ru.push.caudioplayer.core.medialoader;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.medialoader.impl.DefaultMediaInfoDataLoaderService;
import ru.push.caudioplayer.core.playlist.domain.MediaSourceType;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.io.IOException;

import static org.testng.Assert.*;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 4/2/17
 */
public class AudioTrackDataLoaderServiceUnitTest {
  private static final String[] MEDIA_PLAYER_FACTORY_ARGS = new String[] {
      "--quiet"
  };

  private static MediaInfoDataLoaderService mediaInfoDataLoaderService;
  private static CustomMediaPlayerFactory mediaPlayerFactory;

  @DataProvider
  public static Object[][] correctMediaFilesData() throws IOException {
    return new Object[][] {
        {
            new ClassPathResource("audiotracks/01 Yellow Submarine.mp3").getFile().getAbsolutePath(),
            "The Beatles", "Only A Northern Song", "Yellow Submarine"
        },
        {
            new ClassPathResource("audiotracks/02 Hey Bulldog.mp3").getFile().getAbsolutePath(),
            "The Beatles", "Only A Northern Song", "Hey Bulldog"
        },
        {
            new ClassPathResource("audiotracks/03 Eleanor Rigby.mp3").getFile().getAbsolutePath(),
            "The Beatles", "Only A Northern Song", "Eleanor Rigby"
        }
    };
  }

  @DataProvider
  public static Object[][] correctMediaHttpStreamsData() {
    return new Object[][] {
        { "http://ice1.somafm.com/groovesalad-128.mp3", "Groove Salad" },
        { "http://ice1.somafm.com/secretagent-128.mp3", "Secret Agent" },
        { "http://ice1.somafm.com/spacestation-128.mp3", "Space Station Soma" }
    };
  }


  @BeforeClass
  public static void setUpClass() {
    mediaPlayerFactory = new CustomMediaPlayerFactory(MEDIA_PLAYER_FACTORY_ARGS);
    mediaInfoDataLoaderService = new DefaultMediaInfoDataLoaderService(mediaPlayerFactory);
    new NativeDiscovery().discover();     // discover libvlc native libraries
  }

  @AfterClass
  public static void tearDownClass() {
    mediaPlayerFactory.release();
  }

  @Test(dataProvider = "correctMediaFilesData")
  public void testLoadFromFile(String mediaFilePath, String expectedArtist, String expectedAlbum,
                               String expectedTitle) throws Exception {
    PlaylistItem audioTrackData = mediaInfoDataLoaderService.load(new Playlist(), mediaFilePath, MediaSourceType.FILE);

    assertEquals(audioTrackData.getTrackPath(), mediaFilePath, "Unexpected track path.");
    assertEquals(audioTrackData.getArtist(), expectedArtist, "Unexpected artist.");
    assertEquals(audioTrackData.getAlbum(), expectedAlbum, "Unexpected album.");
    assertEquals(audioTrackData.getTitle(), expectedTitle, "Unexpected title.");
    assertEquals(audioTrackData.getSourceType(), MediaSourceType.FILE, "Unexpected source type.");
  }

  @Test(dataProvider = "correctMediaHttpStreamsData")
  public void testLoadFromHttpStream(String mediaHttpStreamPath, String expectedStationName) throws Exception {
		PlaylistItem audioTrackData = mediaInfoDataLoaderService.load(new Playlist(), mediaHttpStreamPath, MediaSourceType.HTTP_STREAM);

    assertEquals(audioTrackData.getTrackPath(), mediaHttpStreamPath, "Unexpected track path.");
    assertTrue(StringUtils.isNotEmpty(audioTrackData.getArtist()), "Artist field must not be empty.");
    assertTrue(StringUtils.isNotEmpty(audioTrackData.getTitle()), "Title field must not be empty.");
    assertTrue(StringUtils.contains(audioTrackData.getAlbum(), expectedStationName),
        "Album field must contains station name.");
    assertEquals(audioTrackData.getSourceType(), MediaSourceType.HTTP_STREAM, "Unexpected source type.");
  }


}