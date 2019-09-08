package ru.push.caudioplayer.core.config.model.view;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class View implements Serializable {

	@NotNull
	@XmlElement
	private PlaylistContainer localPlaylistContainer;

	@NotNull
	@XmlElement
	private PlaylistContainer deezerPlaylistContainer;

	public View() {
	}

	public View(PlaylistContainer localPlaylistContainer, PlaylistContainer deezerPlaylistContainer) {
		this.localPlaylistContainer = localPlaylistContainer;
		this.deezerPlaylistContainer = deezerPlaylistContainer;
	}

	public PlaylistContainer getLocalPlaylistContainer() {
		return localPlaylistContainer;
	}

	public void setLocalPlaylistContainer(PlaylistContainer localPlaylistContainer) {
		this.localPlaylistContainer = localPlaylistContainer;
	}

	public PlaylistContainer getDeezerPlaylistContainer() {
		return deezerPlaylistContainer;
	}

	public void setDeezerPlaylistContainer(PlaylistContainer deezerPlaylistContainer) {
		this.deezerPlaylistContainer = deezerPlaylistContainer;
	}

	@Override
	public String toString() {
		return "View{" +
				"localPlaylistContainer=" + localPlaylistContainer +
				",deezerPlaylistContainer=" + deezerPlaylistContainer +
				'}';
	}
}
