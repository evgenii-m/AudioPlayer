package ru.push.caudioplayer.core.mediaplayer.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/24/17
 */
public class PlaylistData {
  private String name;
  private int position;
  private boolean active;
  private List<MediaInfoData> tracks;

  public PlaylistData(String name, int position, boolean active) {
    this.name = name;
    this.position = position;
    this.active = active;
    this.tracks = new ArrayList<>();
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
