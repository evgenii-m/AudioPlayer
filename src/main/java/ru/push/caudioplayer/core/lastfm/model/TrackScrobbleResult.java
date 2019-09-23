package ru.push.caudioplayer.core.lastfm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class TrackScrobbleResult implements Serializable {

	@XmlElement
	private String track;

	@XmlElement
	private String artist;

	@XmlElement
	private String album;

	@XmlElement
	private String albumArtist;

	@XmlElement
	private Long timestamp;

	@XmlElement
	private String ignoredMessage;


	public TrackScrobbleResult() {
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
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

	public String getAlbumArtist() {
		return albumArtist;
	}

	public void setAlbumArtist(String albumArtist) {
		this.albumArtist = albumArtist;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getIgnoredMessage() {
		return ignoredMessage;
	}

	public void setIgnoredMessage(String ignoredMessage) {
		this.ignoredMessage = ignoredMessage;
	}

	@Override
	public String toString() {
		return "TrackScrobbleResult{" +
				"track='" + track + '\'' +
				", artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", albumArtist='" + albumArtist + '\'' +
				", timestamp=" + timestamp +
				", ignoredMessage='" + ignoredMessage + '\'' +
				'}';
	}
}
