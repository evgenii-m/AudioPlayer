package ru.push.caudioplayer.core.config.model;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class Playlists implements Serializable {

	@XmlElement
	private String activeUid;

	@XmlElement
	private String displayedUid;

	@NotNull
	@XmlElement(name = "playlist")
	private List<PlaylistItem> playlists;

	public Playlists() {
		this.playlists = new ArrayList<>();
	}

	public Playlists(String activeUid, String displayedUid, List<PlaylistItem> playlists) {
		this.activeUid = activeUid;
		this.displayedUid = displayedUid;
		this.playlists = playlists;
	}

	public String getActiveUid() {
		return activeUid;
	}

	public void setActiveUid(String activeUid) {
		this.activeUid = activeUid;
	}

	public String getDisplayedUid() {
		return displayedUid;
	}

	public void setDisplayedUid(String displayedUid) {
		this.displayedUid = displayedUid;
	}

	public List<PlaylistItem> getPlaylists() {
		return playlists;
	}

	public void setPlaylists(List<PlaylistItem> playlists) {
		this.playlists = playlists;
	}

	@Override
	public String toString() {
		return "Playlists{" +
				"activeUid='" + activeUid + '\'' +
				", displayedUid='" + displayedUid + '\'' +
				", playlists=" + playlists +
				'}';
	}
}
