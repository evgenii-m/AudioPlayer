package ru.push.caudioplayer.core.lastfm.domain;

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
public class Date implements Serializable {

	@XmlAttribute(name = "uts")
	private Long uts;

	@XmlValue
	private String dateString;


	public Date() {
	}

	public Date(Long uts, String dateString) {
		this.uts = uts;
		this.dateString = dateString;
	}

	public Long getUts() {
		return uts;
	}

	public void setUts(Long uts) {
		this.uts = uts;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	@Override
	public String toString() {
		return "Date{" +
				"uts=" + uts +
				", dateString='" + dateString + '\'' +
				'}';
	}
}
