package ru.push.caudioplayer.core.config.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum PlaylistType {
	LOCAL, DEEZER;

	public String value() {
		return name();
	}

	public static PlaylistType fromValue(String v) {
		return valueOf(v);
	}
}
