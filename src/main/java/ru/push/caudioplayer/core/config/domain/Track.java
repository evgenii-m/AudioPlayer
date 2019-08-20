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
@XmlRootElement
public class Track implements Serializable {

	@XmlAttribute
	private SourceType sourceType;

	@XmlValue
	private String trackPath;


	public Track() {
	}

	public Track(SourceType sourceType, String trackPath) {
		this.sourceType = sourceType;
		this.trackPath = trackPath;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	public String getTrackPath() {
		return trackPath;
	}

	public void setTrackPath(String trackPath) {
		this.trackPath = trackPath;
	}

	@Override
	public String toString() {
		return "Track{" +
				"sourceType=" + sourceType +
				", trackPath='" + trackPath + '\'' +
				'}';
	}
}
