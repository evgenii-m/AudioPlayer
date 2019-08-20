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
public class PlaylistContainer implements Serializable {

	@NotNull
	@XmlElement
	private Columns columns;


	public PlaylistContainer() {
	}

	public PlaylistContainer(Columns columns) {
		this.columns = columns;
	}

	public Columns getColumns() {
		return columns;
	}

	public void setColumns(Columns columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "PlaylistContainer{" +
				"columns=" + columns +
				'}';
	}
}
