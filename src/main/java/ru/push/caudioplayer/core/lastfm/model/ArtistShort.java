package ru.push.caudioplayer.core.lastfm.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class ArtistShort implements Serializable {

	@XmlAttribute(name = "mbid")
	private String mbid;

	@XmlValue
	private String name;


	public ArtistShort() {
	}

	public ArtistShort(String mbid, String name) {
		this.mbid = mbid;
		this.name = name;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ArtistShort{" +
				"mbid='" + mbid + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
