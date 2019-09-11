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

	public View() {
	}

	public View(PlaylistContainer localPlaylistContainer) {
		this.localPlaylistContainer = localPlaylistContainer;
	}

	public PlaylistContainer getLocalPlaylistContainer() {
		return localPlaylistContainer;
	}

	public void setLocalPlaylistContainer(PlaylistContainer localPlaylistContainer) {
		this.localPlaylistContainer = localPlaylistContainer;
	}

	@Override
	public String toString() {
		return "View{" +
				"localPlaylistContainer=" + localPlaylistContainer +
				'}';
	}
}
