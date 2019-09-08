package ru.push.caudioplayer.core.playlist.model;

import org.apache.commons.lang3.StringUtils;


public class PlaylistTrack {

	private MediaSourceType sourceType;
	private String artist;
	private String album;
	private String date;
	private String title;
	private String trackId;
	private String trackNumber;
	private long length;
	private String trackPath;
	private Playlist playlist;


	public PlaylistTrack(Playlist playlist) {
		this.trackPath = StringUtils.EMPTY;
		this.sourceType = MediaSourceType.FILE;
		this.playlist = playlist;
	}

	public PlaylistTrack(MediaSourceType sourceType, String artist, String album, String date, String title,
											 String trackId, String trackNumber, long length, String trackPath) {
		this.sourceType = sourceType;
		this.artist = artist;
		this.album = album;
		this.date = date;
		this.title = title;
		this.trackId = trackId;
		this.trackNumber = trackNumber;
		this.length = length;
		this.trackPath = trackPath;
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

	public Playlist getPlaylist() {
		return playlist;
	}

	protected void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PlaylistTrack that = (PlaylistTrack) o;

		if (length != that.length) return false;
		if (trackPath != null ? !trackPath.equals(that.trackPath) : that.trackPath != null) return false;
		if (sourceType != that.sourceType) return false;
		if (artist != null ? !artist.equals(that.artist) : that.artist != null) return false;
		if (album != null ? !album.equals(that.album) : that.album != null) return false;
		if (date != null ? !date.equals(that.date) : that.date != null) return false;
		if (title != null ? !title.equals(that.title) : that.title != null) return false;
		if (trackId != null ? !trackId.equals(that.trackId) : that.trackId != null) return false;
		return trackNumber != null ? trackNumber.equals(that.trackNumber) : that.trackNumber == null;

	}

	@Override
	public int hashCode() {
		int result = trackPath != null ? trackPath.hashCode() : 0;
		result = 31 * result + (sourceType != null ? sourceType.hashCode() : 0);
		result = 31 * result + (artist != null ? artist.hashCode() : 0);
		result = 31 * result + (album != null ? album.hashCode() : 0);
		result = 31 * result + (date != null ? date.hashCode() : 0);
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (trackId != null ? trackId.hashCode() : 0);
		result = 31 * result + (trackNumber != null ? trackNumber.hashCode() : 0);
		result = 31 * result + (int) (length ^ (length >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "PlaylistTrack{" +
				"trackPath='" + trackPath + '\'' +
				", sourceType=" + sourceType +
				", artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", date='" + date + '\'' +
				", title='" + title + '\'' +
				", trackId='" + trackId + '\'' +
				", trackNumber='" + trackNumber + '\'' +
				", length=" + length +
				", playlistUid=" + ((playlist != null) ? playlist.getUid() : null) +
				'}';
	}
}
