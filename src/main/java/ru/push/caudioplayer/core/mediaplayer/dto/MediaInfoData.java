package ru.push.caudioplayer.core.mediaplayer.dto;

import org.apache.commons.lang3.StringUtils;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/13/17
 */
public class MediaInfoData {
  private String trackPath;
  private MediaSourceType sourceType;
  private String artist;
  private String album;
  private String date;
  private String title;
  private String trackId;
  private String trackNumber;
  private long length;

  public MediaInfoData() {
    this.trackPath = StringUtils.EMPTY;
    this.sourceType = MediaSourceType.FILE;
  }

  public MediaInfoData(String trackPath) {
    this.trackPath = trackPath;
    this.sourceType = MediaSourceType.FILE;
  }

  public String getTrackPath() {
    return trackPath;
  }

  public void setTrackPath(String trackPath) {
    this.trackPath = trackPath;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getAlbum() {
    return album;
  }

  public void setAlbum(String album) {
    this.album = album;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTrackId() {
    return trackId;
  }

  public void setTrackId(String trackId) {
    this.trackId = trackId;
  }

  public String getTrackNumber() {
    return trackNumber;
  }

  public void setTrackNumber(String trackNumber) {
    this.trackNumber = trackNumber;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  public MediaSourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(MediaSourceType sourceType) {
    this.sourceType = sourceType;
  }
}
