package ru.push.caudioplayer.core.converter.domain;

import ru.push.caudioplayer.core.config.domain.PlaylistType;

import javax.validation.constraints.NotNull;
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
public class PlaylistExportData implements Serializable {

	@NotNull
	@XmlAttribute
	private String name;

	@NotNull
	@XmlAttribute(name ="type")
	private PlaylistType playlistType;

	@XmlElement(name = "track")
	private List<TrackExportData> tracks;

	public PlaylistExportData() {
	}

	public PlaylistExportData(String name, PlaylistType playlistType, List<TrackExportData> tracks) {
		this.name = name;
		this.playlistType = playlistType;
		this.tracks = tracks;
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

	public List<TrackExportData> getTracks() {
		return tracks;
	}

	public void setTracks(List<TrackExportData> tracks) {
		this.tracks = tracks;
	}

	@Override
	public String toString() {
		return "PlaylistExportData{" +
				"name='" + name + '\'' +
				", playlistType=" + playlistType +
				", tracks=" + tracks +
				'}';
	}
}
