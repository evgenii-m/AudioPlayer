package ru.push.caudioplayer.core.config.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum SourceType {
	FILE, HTTP_STREAM, DEEZER_MEDIA;

	public String value() {
		return name();
	}

	public static SourceType fromValue(String v) {
		return valueOf(v);
	}
}
