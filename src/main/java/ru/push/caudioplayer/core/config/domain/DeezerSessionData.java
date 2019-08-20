package ru.push.caudioplayer.core.config.domain;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "deezer")
public class DeezerSessionData implements Serializable {

	@NotNull
	@XmlElement
	private String accessToken;


	public DeezerSessionData() {
	}

	public DeezerSessionData(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public String toString() {
		return "DeezerSessionData{" +
				"accessToken='" + accessToken + '\'' +
				'}';
	}
}
