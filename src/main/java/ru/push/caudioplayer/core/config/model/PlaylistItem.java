package ru.push.caudioplayer.core.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "playlist")
public class PlaylistItem implements Serializable {

	@XmlValue
	private String playlistUid;

	@XmlAttribute
	private long position;

	@XmlAttribute
	private String title;

	public PlaylistItem(){
	}

	public PlaylistItem(String playlistUid, long position, String title) {
		this.playlistUid = playlistUid;
		this.position = position;
		this.title = title;
	}

	public String getPlaylistUid() {
		return playlistUid;
	}

	public void setPlaylistUid(String playlistUid) {
		this.playlistUid = playlistUid;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "PlaylistItem{" +
				"playlistUid='" + playlistUid + '\'' +
				", position=" + position +
				", title='" + title + '\'' +
				'}';
	}
}
