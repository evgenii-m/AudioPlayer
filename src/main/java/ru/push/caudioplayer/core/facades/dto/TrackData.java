package ru.push.caudioplayer.core.facades.dto;

import java.util.Objects;

public class TrackData {
	private String artist;
	private String album;
	private String date;
	private String title;
	private String trackNumber;
	private long length;

	public TrackData(String artist, String album, String date, String title, String trackNumber, long length) {
		this.artist = artist;
		this.album = album;
		this.date = date;
		this.title = title;
		this.trackNumber = trackNumber;
		this.length = length;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrackData trackData = (TrackData) o;
		return length == trackData.length &&
				Objects.equals(artist, trackData.artist) &&
				Objects.equals(album, trackData.album) &&
				Objects.equals(date, trackData.date) &&
				Objects.equals(title, trackData.title);
	}

	@Override
	public int hashCode() {

		return Objects.hash(artist, album, date, title, length);
	}

	@Override
	public String toString() {
		return "TrackData{" +
				"artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", date='" + date + '\'' +
				", title='" + title + '\'' +
				", trackNumber='" + trackNumber + '\'' +
				", length=" + length +
				'}';
	}
}
