package ru.push.caudioplayer.core.playlist.dao.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "PLAYLIST_ITEM")
public class PlaylistItemEntity implements Serializable {

	@NotNull
	@Id
	@Column(nullable = false)
	private String uid;

	@NotNull
	@Column(nullable = false)
	private String sourceType;

	@NotNull
	@Column
	private String artist;

	@Column
	private String album;

	@Column
	private String date;

	@NotNull
	@Column
	private String title;

	@Column
	private String trackNumber;

	@Column(nullable = false)
	private long length;

	@Column
	private String trackPath;

	@NotNull
	@ManyToOne
	@JoinColumn(name = "PLAYLIST_UID", nullable = false)
	private PlaylistEntity playlist;


	public PlaylistItemEntity() {
	}

	public PlaylistItemEntity(String uid, String sourceType, String artist, String album, String date, String title,
														String trackNumber, long length, String trackPath, PlaylistEntity playlist) {
		this.uid = uid;
		this.sourceType = sourceType;
		this.artist = artist;
		this.album = album;
		this.date = date;
		this.title = title;
		this.trackNumber = trackNumber;
		this.length = length;
		this.trackPath = trackPath;
		this.playlist = playlist;
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

	public void setUid(String uid) {
		this.uid = uid;
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

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public PlaylistEntity getPlaylist() {
		return playlist;
	}

	public void setPlaylist(PlaylistEntity playlist) {
		this.playlist = playlist;
	}

	@Override
	public String toString() {
		return "PlaylistItemEntity{" +
				"uid='" + uid + '\'' +
				", trackPath='" + trackPath + '\'' +
				", sourceType=" + sourceType +
				", artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", date='" + date + '\'' +
				", title='" + title + '\'' +
				", trackNumber='" + trackNumber + '\'' +
				", length=" + length +
				'}';
	}
}
