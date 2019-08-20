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
@XmlRootElement(name = "lastfm")
public class LastfmSessionData implements Serializable {

	@NotNull
	@XmlElement
	private String username;

	@NotNull
	@XmlElement
	private String sessionKey;


	public LastfmSessionData() {
	}

	public LastfmSessionData(String username, String sessionKey) {
		this.username = username;
		this.sessionKey = sessionKey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	@Override
	public String toString() {
		return "LastfmSessionData{" +
				"username='" + username + '\'' +
				", sessionKey='" + sessionKey + '\'' +
				'}';
	}
}
