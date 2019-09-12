package ru.push.caudioplayer.core.facades.dto;

import java.util.Objects;

public class TrackData {
	private String trackUid;
	private String playlistUid;
	private String artist;
	private String album;
	private String date;
	private String title;
	private String trackNumber;
	private long length;
	private boolean nowPlaying;
	private String playlistTitle;

	public TrackData(String trackUid, String playlistUid, String artist, String album,
									 String date, String title, String trackNumber, long length, boolean nowPlaying,
									 String playlistTitle) {
		this.trackUid = trackUid;
		this.playlistUid = playlistUid;
		this.artist = artist;
		this.album = album;
		this.date = date;
		this.title = title;
		this.trackNumber = trackNumber;
		this.length = length;
		this.nowPlaying = nowPlaying;
		this.playlistTitle = playlistTitle;
	}

	public String getTrackUid() {
		return trackUid;
	}

	public void setTrackUid(String trackUid) {
		this.trackUid = trackUid;
	}

	public String getPlaylistUid() {
		return playlistUid;
	}

	public void setPlaylistUid(String playlistUid) {
		this.playlistUid = playlistUid;
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

	public boolean isNowPlaying() {
		return nowPlaying;
	}

	public void setNowPlaying(boolean nowPlaying) {
		this.nowPlaying = nowPlaying;
	}

	public String getPlaylistTitle() {
		return playlistTitle;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TrackData trackData = (TrackData) o;
		return Objects.equals(trackUid, trackData.trackUid) &&
				Objects.equals(playlistUid, trackData.playlistUid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(trackUid, playlistUid);
	}

	@Override
	public String toString() {
		return "TrackData{" +
				"trackUid='" + trackUid + '\'' +
				", playlistUid='" + playlistUid + '\'' +
				", artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", date='" + date + '\'' +
				", title='" + title + '\'' +
				", trackNumber='" + trackNumber + '\'' +
				", length=" + length +
				", nowPlaying=" + nowPlaying +
				", playlistTitle='" + playlistTitle + '\'' +
				'}';
	}
}
