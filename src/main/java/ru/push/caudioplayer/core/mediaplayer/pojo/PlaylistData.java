package ru.push.caudioplayer.core.mediaplayer.pojo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/24/17
 */
public class PlaylistData {
  private static final String DEFAULT_PLAYLIST_NAME = "New playlist";

  private String name;
  private List<MediaInfoData> tracks;

  public static String getNewPlaylistName() {
    String currentTimeString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
    return DEFAULT_PLAYLIST_NAME + " " + currentTimeString;
  }

  public PlaylistData() {
    this(getNewPlaylistName());
  }

  public PlaylistData(String name) {
    this(name, new ArrayList<>());
    this.name = name;
  }

  public PlaylistData(String name, List<MediaInfoData> tracks) {
    this.name = name;
    this.tracks = tracks;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MediaInfoData> getTracks() {
    return tracks;
  }

  public void setTracks(List<MediaInfoData> tracks) {
    this.tracks = tracks;
  }
}
