package ru.push.caudioplayer.core.playlist.dao.model;

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
	private String trackId;

	@XmlAttribute
	private String trackNumber;

	@XmlAttribute
	private long length;

	@XmlValue
	private String trackPath;


	public PlaylistItemEntity() {
	}

	public PlaylistItemEntity(String sourceType, String artist, String album, String date, String title,
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

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public String toString() {
		return "PlaylistItemEntity{" +
				"trackPath='" + trackPath + '\'' +
				", sourceType=" + sourceType +
				", artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", date='" + date + '\'' +
				", title='" + title + '\'' +
				", trackId='" + trackId + '\'' +
				", trackNumber='" + trackNumber + '\'' +
				", length=" + length +
				'}';
	}
}
