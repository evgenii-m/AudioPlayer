package ru.push.caudioplayer.core.mediaplayer.pojo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/24/17
 */
public class PlaylistData {
  private static final String DEFAULT_PLAYLIST_NAME = "New playlist";

  private final String uid;
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
  }

  public PlaylistData(String name, List<MediaInfoData> tracks) {
    this.uid = UUID.randomUUID().toString();
    this.name = name;
    this.tracks = tracks;
  }


  public PlaylistData(String uid, String name, List<MediaInfoData> tracks) {
    this.uid = uid;
    this.name = name;
    this.tracks = tracks;
  }

  public String getUid() {
    return uid;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PlaylistData that = (PlaylistData) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    return tracks != null ? tracks.equals(that.tracks) : that.tracks == null;

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (tracks != null ? tracks.hashCode() : 0);
    return result;
  }
}
