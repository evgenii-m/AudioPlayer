package ru.push.caudioplayer.core.lastfm.pojo;

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
public class Album implements Serializable {

	@XmlAttribute(name = "mbid")
	private String mbid;

	@XmlValue
	private String name;


	public Album() {
	}

	public Album(String mbid, String name) {
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
		return "Album{" +
				"mbid='" + mbid + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
