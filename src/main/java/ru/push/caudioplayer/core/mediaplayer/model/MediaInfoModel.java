package ru.push.caudioplayer.core.mediaplayer.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/13/17
 */
public class MediaInfoModel {
  private String trackPath;
  private MediaSourceType sourceType;
  private String artist;
  private String album;
  private String date;
  private String title;
  private String trackId;
  private String trackNumber;
  private long length;

  public MediaInfoModel() {
    this.trackPath = StringUtils.EMPTY;
    this.sourceType = MediaSourceType.FILE;
  }

  public MediaInfoModel(String trackPath) {
    this.trackPath = trackPath;
    this.sourceType = MediaSourceType.FILE;
  }

  private MediaInfoModel(Builder builder) {
    this.trackPath = builder.trackPath;
    this.sourceType = builder.sourceType;
    this.artist = builder.artist;
    this.album = builder.album;
    this.date = builder.date;
    this.title = builder.title;
    this.trackId = builder.trackId;
    this.trackNumber = builder.trackNumber;
    this.length = builder.length;
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

  public static class Builder {
    private String trackPath;
    private MediaSourceType sourceType;
    private String artist;
    private String album;
    private String date;
    private String title;
    private String trackId;
    private String trackNumber;
    private long length;

    public Builder trackPath(String trackPath) {
      this.trackPath = trackPath;
      return this;
    }

    public Builder sourceType(MediaSourceType sourceType) {
      this.sourceType = sourceType;
      return this;
    }

    public Builder artist(String artist) {
      this.artist = artist;
      return this;
    }

    public Builder album(String album) {
      this.album = album;
      return this;
    }

    public Builder date(String date) {
      this.date = date;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder trackId(String trackId) {
      this.trackId = trackId;
      return this;
    }

    public Builder trackNumber(String trackNumber) {
      this.trackNumber = trackNumber;
      return this;
    }

    public Builder length(long length) {
      this.length = length;
      return this;
    }

    public MediaInfoModel build() {
      return new MediaInfoModel(this);
    }
  }
}
