package ru.push.caudioplayer.core.lastfm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "lfm")
public class LastFmResponse implements Serializable {

	@XmlAttribute(name = "status")
	private String status;

	@XmlElement(name = "recenttracks")
	private RecentTracks recentTracks;


	public LastFmResponse() {
	}

	public LastFmResponse(String status, RecentTracks recentTracks) {
		this.status = status;
		this.recentTracks = recentTracks;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public RecentTracks getRecentTracks() {
		return recentTracks;
	}

	public void setRecentTracks(RecentTracks recentTracks) {
		this.recentTracks = recentTracks;
	}

	@Override
	public String toString() {
		return "LastFmResponse{" +
				"status='" + status + '\'' +
				", recentTracks=" + recentTracks +
				'}';
	}
}
