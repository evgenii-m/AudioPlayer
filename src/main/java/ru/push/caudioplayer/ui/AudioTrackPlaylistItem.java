package ru.push.caudioplayer.ui;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/25/17
 * todo: remove class and make setCellValueFactory as ru.push.caudioplayer.controller.LastFmPanelController#setRecentTracksContainerColumns()
 */
public class AudioTrackPlaylistItem {
  private final SimpleStringProperty number;
  private final SimpleStringProperty artist;
  private final SimpleStringProperty album;
  private final SimpleStringProperty title;
  private final SimpleStringProperty length;

	public AudioTrackPlaylistItem() {
		this.number = new SimpleStringProperty();
		this.artist = new SimpleStringProperty();
		this.album = new SimpleStringProperty();
		this.title = new SimpleStringProperty();
		this.length = new SimpleStringProperty();
	}

  public AudioTrackPlaylistItem(String number, String artist, String album, String title, String length) {
    this.number = new SimpleStringProperty(number);
    this.artist = new SimpleStringProperty(artist);
    this.album = new SimpleStringProperty(album);
    this.title = new SimpleStringProperty(title);
    this.length = new SimpleStringProperty(length);
  }

  public String getNumber() {
    return number.get();
  }

  public SimpleStringProperty numberProperty() {
    return number;
  }

  public void setNumber(String number) {
    this.number.set(number);
  }

  public String getArtist() {
    return artist.get();
  }

  public SimpleStringProperty artistProperty() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist.set(artist);
  }

  public String getAlbum() {
    return album.get();
  }

  public SimpleStringProperty albumProperty() {
    return album;
  }

  public void setAlbum(String album) {
    this.album.set(album);
  }

  public String getTitle() {
    return title.get();
  }

  public SimpleStringProperty titleProperty() {
    return title;
  }

  public void setTitle(String title) {
    this.title.set(title);
  }

  public String getLength() {
    return length.get();
  }

  public SimpleStringProperty lengthProperty() {
    return length;
  }

  public void setLength(String length) {
    this.length.set(length);
  }
}
