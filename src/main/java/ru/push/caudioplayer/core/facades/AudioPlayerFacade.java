package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/26/17
 */
public interface AudioPlayerFacade {

  void addListener(AudioPlayerEventListener listener);

  void removeListener(AudioPlayerEventListener listener);

  List<PlaylistData> getPlaylists();

  void createNewPlaylist();

  void playMedia(int trackPosition);
}
