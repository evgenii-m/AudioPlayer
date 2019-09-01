package ru.push.caudioplayer.core.converter.domain;

import ru.push.caudioplayer.core.config.domain.SourceType;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "track")
public class TrackExportData implements Serializable {

	@NotNull
	@XmlValue
	private String uri;  // file path or deezer URI

	@NotNull
	@XmlAttribute
	private SourceType sourceType;


	public TrackExportData() {
	}

	public TrackExportData(String uri, SourceType sourceType) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public String toString() {
		return "TrackExportData{" +
				"uri='" + uri + '\'' +
				", sourceType=" + sourceType +
				'}';
	}
}
