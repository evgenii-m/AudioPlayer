package ru.push.caudioplayer.core.config.domain;

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

	public PlaylistItem(){
	}

	public PlaylistItem(String playlistUid, long position) {
		this.playlistUid = playlistUid;
		this.position = position;
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

	@Override
	public String toString() {
		return "PlaylistItem{" +
				"playlistUid='" + playlistUid + '\'' +
				", position='" + position + '\'' +
				'}';
	}
}
