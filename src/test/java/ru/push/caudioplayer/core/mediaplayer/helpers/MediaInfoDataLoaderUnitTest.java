package ru.push.caudioplayer.core.mediaplayer.helpers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.helpers.impl.DefaultMediaInfoDataLoader;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaSourceType;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.io.IOException;

import static org.testng.Assert.*;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 4/2/17
 */
public class MediaInfoDataLoaderUnitTest {
  private static final String[] MEDIA_PLAYER_FACTORY_ARGS = new String[] {
      "--quiet"
  };

  private static MediaInfoDataLoader mediaInfoDataLoader;
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
    mediaInfoDataLoader = new DefaultMediaInfoDataLoader(mediaPlayerFactory);
    new NativeDiscovery().discover();     // discover libvlc native libraries
  }

  @AfterClass
  public static void tearDownClass() {
    mediaPlayerFactory.release();
  }

  @Test(dataProvider = "correctMediaFilesData")
  public void testLoadFromFile(String mediaFilePath, String expectedArtist, String expectedAlbum,
                               String expectedTitle) throws Exception {
    MediaInfoData mediaInfoData = mediaInfoDataLoader.load(mediaFilePath, MediaSourceType.FILE);

    assertEquals(mediaInfoData.getTrackPath(), mediaFilePath, "Unexpected track path.");
    assertEquals(mediaInfoData.getArtist(), expectedArtist, "Unexpected artist.");
    assertEquals(mediaInfoData.getAlbum(), expectedAlbum, "Unexpected album.");
    assertEquals(mediaInfoData.getTitle(), expectedTitle, "Unexpected title.");
    assertEquals(mediaInfoData.getSourceType(), MediaSourceType.FILE, "Unexpected source type.");
  }

  @Test(dataProvider = "correctMediaHttpStreamsData")
  public void testLoadFromHttpStream(String mediaHttpStreamPath, String expectedStationName) throws Exception {
    MediaInfoData mediaInfoData = mediaInfoDataLoader.load(mediaHttpStreamPath, MediaSourceType.HTTP_STREAM);

    assertEquals(mediaInfoData.getTrackPath(), mediaHttpStreamPath, "Unexpected track path.");
    assertTrue(StringUtils.isNotEmpty(mediaInfoData.getArtist()), "Artist field must not be empty.");
    assertTrue(StringUtils.isNotEmpty(mediaInfoData.getTitle()), "Title field must not be empty.");
    assertTrue(StringUtils.contains(mediaInfoData.getAlbum(), expectedStationName),
        "Album field must contains station name.");
    assertEquals(mediaInfoData.getSourceType(), MediaSourceType.HTTP_STREAM, "Unexpected source type.");
  }


}