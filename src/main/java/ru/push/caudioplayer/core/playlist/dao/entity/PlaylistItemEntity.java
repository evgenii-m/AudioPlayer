package ru.push.caudioplayer.core.playlist.dao.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "item")
public class PlaylistItemEntity implements Serializable {

	@XmlAttribute
	private String uid;

	@XmlAttribute
	private String sourceType;

	@XmlAttribute
	private String artist;

	@XmlAttribute
	private String album;

	@XmlAttribute
	private String date;

	@XmlAttribute
	private String title;

	@XmlAttribute
	private String trackNumber;

	@XmlAttribute
	private long length;

	@XmlValue
	private String trackPath;


	public PlaylistItemEntity() {
	}

	public PlaylistItemEntity(String uid, String sourceType, String trackPath) {
		this.uid = uid;
		this.sourceType = sourceType;
		this.trackPath = trackPath;
	}

	public PlaylistItemEntity(String uid, String sourceType, String artist, String album, String date, String title,
														String trackNumber, long length, String trackPath) {
		this.uid = uid;
		this.sourceType = sourceType;
		this.artist = artist;
		this.album = album;
		this.date = date;
		this.title = title;
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
