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
@XmlRootElement
public class Playlist implements Serializable {

	@XmlAttribute
	private String uid;

	@XmlAttribute
	private String name;

	@XmlAttribute
	private long position;

	@XmlElement(name = "track")
	private List<Track> tracks;

	public Playlist() {
	}

	public Playlist(String uid, String name, long position, List<Track> tracks) {
		this.uid = uid;
		this.name = name;
		this.position = position;
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

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	@Override
	public String toString() {
		return "Playlist{" +
				"uid='" + uid + '\'' +
				", name='" + name + '\'' +
				", position=" + position +
				", tracks=" + tracks +
				'}';
	}
}
