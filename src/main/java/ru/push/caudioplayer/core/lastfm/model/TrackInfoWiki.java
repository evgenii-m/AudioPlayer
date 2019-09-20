package ru.push.caudioplayer.core.lastfm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "wiki")
public class TrackInfoWiki implements Serializable {

	@XmlElement
	private String published;

	@XmlElement
	private String summary;

	@XmlElement
	private String content;

	public TrackInfoWiki() {
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "TrackInfoWiki{" +
				"published='" + published + '\'' +
				", summary='" + summary + '\'' +
				", content='" + content + '\'' +
				'}';
	}
}
