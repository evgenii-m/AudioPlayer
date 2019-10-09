package ru.push.caudioplayer.core.playlist.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;


public class PlaylistTrack {
	private final String uid;
	private MediaSourceType sourceType;
	private String trackPath;
	private Playlist playlist;
	private String artist;
	private String album;
	private String date;
	private String title;
	private String trackNumber;
	private long length;		// in milliseconds
	private boolean nowPlaying;


	public PlaylistTrack(String uid, Playlist playlist) {
		this(uid, MediaSourceType.FILE, StringUtils.EMPTY, playlist);
	}

	public PlaylistTrack(String uid, MediaSourceType sourceType, String trackPath, Playlist playlist) {
		this.uid = uid;
		this.sourceType = sourceType;
		this.trackPath = trackPath;
		this.playlist = playlist;
		this.nowPlaying = false;
	}

	public PlaylistTrack(String uid, MediaSourceType sourceType, String trackPath, String artist,
											 String album, String date, String title, String trackNumber, long length) {
		this(uid, sourceType, trackPath, null, artist, album, date, title, trackNumber, length);
	}

	public PlaylistTrack(String uid, MediaSourceType sourceType, String trackPath, Playlist playlist,
											 String artist, String album, String date, String title, String trackNumber, long length) {
		this.uid = uid;
		this.sourceType = sourceType;
		this.trackPath = trackPath;
		this.playlist = playlist;
		this.artist = artist;
		this.album = album;
		this.date = date;
		this.title = title;
		this.trackNumber = trackNumber;
		this.length = length;
		this.nowPlaying = false;
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

	public String getUid() {
		return uid;
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

	public Playlist getPlaylist() {
		return playlist;
	}

	protected void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	public boolean isNowPlaying() {
		return nowPlaying;
	}

	public void setNowPlaying(boolean nowPlaying) {
		this.nowPlaying = nowPlaying;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlaylistTrack that = (PlaylistTrack) o;
		return Objects.equals(uid, that.uid) &&
				sourceType == that.sourceType &&
				Objects.equals(trackPath, that.trackPath) &&
				Objects.equals(playlist, that.playlist);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uid, sourceType, trackPath, playlist);
	}

	@Override
	public String toString() {
		return "PlaylistTrack{" +
				"uid='" + uid + '\'' +
				", trackPath='" + trackPath + '\'' +
				", sourceType=" + sourceType +
				", artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", date='" + date + '\'' +
				", title='" + title + '\'' +
				", trackNumber='" + trackNumber + '\'' +
				", length=" + length +
				", playlistUid=" + ((playlist != null) ? playlist.getUid() : null) +
				'}';
	}
}
