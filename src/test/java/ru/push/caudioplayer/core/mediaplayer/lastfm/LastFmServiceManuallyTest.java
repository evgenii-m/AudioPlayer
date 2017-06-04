package ru.push.caudioplayer.core.mediaplayer.lastfm;

import org.testng.annotations.Test;
import ru.push.caudioplayer.core.mediaplayer.lastfm.impl.DefaultLastFmService;

/**
 * This test class used only for calling service methods to make Last.FM API requests,
 * but the results are checked manually.
 *
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
@Test
public class LastFmServiceManuallyTest {

  private final LastFmService lastFmService;
//  private final LastFmUserData userData;

  LastFmServiceManuallyTest() {
    lastFmService = new DefaultLastFmService();
//    userData = new LastFmUserData();
  }

  @Test
  public void testUpdateNowPlaying() {
    String artistName = "The Beatles";
    String trackTitle = "Yellow Submarine";
//    lastFmService.updateNowPlaying();
  }

}
