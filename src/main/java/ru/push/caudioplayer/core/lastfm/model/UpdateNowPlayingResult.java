package ru.push.caudioplayer.core.lastfm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class UpdateNowPlayingResult implements Serializable {

	@XmlElement
	private String track;

	@XmlElement
	private String artist;

	@XmlElement
	private String album;

	@XmlElement
	private String albumArtist;

	@XmlElement
	private String ignoredMessage;


	public UpdateNowPlayingResult() {
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

	public String getIgnoredMessage() {
		return ignoredMessage;
	}

	public void setIgnoredMessage(String ignoredMessage) {
		this.ignoredMessage = ignoredMessage;
	}

	@Override
	public String toString() {
		return "UpdateNowPlayingResult{" +
				"track='" + track + '\'' +
				", artist='" + artist + '\'' +
				", album='" + album + '\'' +
				", albumArtist='" + albumArtist + '\'' +
				", ignoredMessage='" + ignoredMessage + '\'' +
				'}';
	}
}
