package ru.push.caudioplayer.core.config.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "playlistConfiguration")
public class PlaylistConfig implements Serializable {

	@XmlAttribute
	private String uid;

	@XmlAttribute
	private String name;

	@XmlAttribute(name ="type")
	private PlaylistType playlistType;

	@XmlAttribute
	private String link;

	@XmlElement(name = "track")
	private List<Track> tracks;

	public PlaylistConfig() {
	}

	public PlaylistConfig(String uid, String name, PlaylistType playlistType, String link, List<Track> tracks) {
		this.uid = uid;
		this.name = name;
		this.playlistType = playlistType;
		this.link = link;
		this.tracks = tracks;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PlaylistType getPlaylistType() {
		return playlistType;
	}

	public void setPlaylistType(PlaylistType playlistType) {
		this.playlistType = playlistType;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	@Override
	public String toString() {
		return "PlaylistConfig{" +
				"uid='" + uid + '\'' +
				", name='" + name + '\'' +
				", playlistType=" + playlistType +
				", link='" + link + '\'' +
				", tracks=" + tracks +
				'}';
	}
}
