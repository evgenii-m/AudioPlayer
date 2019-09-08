package ru.push.caudioplayer.core.config.model.view;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement
public class Column implements Serializable {

	@NotNull
	@XmlAttribute
	private String name;

	@NotNull
	@XmlAttribute
	private String title;

	@NotNull
	@XmlAttribute
	private double width;

	public Column() {
	}

	public Column(String name, String title, double width) {
		this.name = name;
		this.title = title;
		this.width = width;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	@Override
	public String toString() {
		return "Column{" +
				"name='" + name + '\'' +
				", title='" + title + '\'' +
				", width=" + width +
				'}';
	}
}
