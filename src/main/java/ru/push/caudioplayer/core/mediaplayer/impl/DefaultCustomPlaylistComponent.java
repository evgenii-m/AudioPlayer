package ru.push.caudioplayer.core.mediaplayer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomMediaPlayerFactory;
import ru.push.caudioplayer.core.mediaplayer.CustomPlaylistComponent;
import uk.co.caprica.vlcj.medialist.MediaList;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public class DefaultCustomPlaylistComponent implements CustomPlaylistComponent {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultCustomPlaylistComponent.class);

  private final MediaList mediaList;

  private final CustomMediaPlayerFactory mediaPlayerFactory;

  public DefaultCustomPlaylistComponent(CustomMediaPlayerFactory mediaPlayerFactory) {
    this.mediaPlayerFactory = mediaPlayerFactory;
    this.mediaList = mediaPlayerFactory.newMediaList();
  }

  public final MediaList getMediaList() {
    return mediaList;
  }

  @Override
  public void releaseComponent() {
    LOG.debug("releaseComponent");
    mediaList.release();
  }
}
