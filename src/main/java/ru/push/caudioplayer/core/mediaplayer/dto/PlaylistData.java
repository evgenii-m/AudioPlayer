package ru.push.caudioplayer.core.mediaplayer.dto;

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
  private int position;
  private boolean active;
  private List<MediaInfoData> tracks;

  public static String getNewPlaylistName() {
    String currentTimeString = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
    return DEFAULT_PLAYLIST_NAME + " " + currentTimeString;
  }

  public PlaylistData(String name, int position, boolean active) {
    this.name = name;
    this.position = position;
    this.active = active;
    this.tracks = new ArrayList<>();
  }

  public PlaylistData(int position) {
    this(getNewPlaylistName(), position, true);
  }

  public PlaylistData(String name, int position, List<MediaInfoData> tracks, boolean active) {
    this.name = name;
    this.position = position;
    this.active = active;
    this.tracks = tracks;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public List<MediaInfoData> getTracks() {
    return tracks;
  }

  public void setTracks(List<MediaInfoData> tracks) {
    this.tracks = tracks;
  }
}
