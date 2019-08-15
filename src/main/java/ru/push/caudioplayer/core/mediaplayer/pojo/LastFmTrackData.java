package ru.push.caudioplayer.core.mediaplayer.pojo;


import java.util.Date;
import java.util.Objects;

public class LastFmTrackData {
	private String artist;
	private String album;
	private String title;
	private boolean nowPlaying;
	private Date scrobbleDate;

	public LastFmTrackData(String artist, String album, String title, boolean nowPlaying, Date scrobbleDate) {
		this.artist = artist;
		this.album = album;
		this.title = title;
		this.nowPlaying = nowPlaying;
		this.scrobbleDate = scrobbleDate;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isNowPlaying() {
		return nowPlaying;
	}

	public void setNowPlaying(boolean nowPlaying) {
		this.nowPlaying = nowPlaying;
	}

	public Date getScrobbleDate() {
		return scrobbleDate;
	}

	public void setScrobbleDate(Date scrobbleDate) {
		this.scrobbleDate = scrobbleDate;
	}

	public boolean isValid() {
		return (artist != null) && (title != null) && (scrobbleDate != null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LastFmTrackData that = (LastFmTrackData) o;
		return nowPlaying == that.nowPlaying &&
				Objects.equals(artist, that.artist) &&
				Objects.equals(album, that.album) &&
				Objects.equals(title, that.title) &&
				Objects.equals(scrobbleDate, that.scrobbleDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist, album, title, nowPlaying, scrobbleDate);
	}

	@Override
	public String toString() {
		return "LastFmTrackData{" +
				"artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", title='" + title + '\'' +
				", nowPlaying=" + nowPlaying +
				", scrobbleDate=" + scrobbleDate +
				'}';
	}
}
