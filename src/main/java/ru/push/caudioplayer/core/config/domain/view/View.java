package ru.push.caudioplayer.core.config.domain.view;

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
	private PlaylistContainer playlistContainer;

	public View() {
	}

	public View(PlaylistContainer playlistContainer) {
		this.playlistContainer = playlistContainer;
	}

	public PlaylistContainer getPlaylistContainer() {
		return playlistContainer;
	}

	public void setPlaylistContainer(PlaylistContainer playlistContainer) {
		this.playlistContainer = playlistContainer;
	}

	@Override
	public String toString() {
		return "View{" +
				"playlistContainer=" + playlistContainer +
				'}';
	}
}
