package ru.push.caudioplayer.core.config.model.view;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class Columns implements Serializable {

	@NotNull
	@XmlElement(name = "column")
	private List<Column> columns;

	public Columns() {
	}

	public Columns(List<Column> columns) {
		this.columns = columns;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "Columns{" +
				"columns=" + columns +
				'}';
	}
}
