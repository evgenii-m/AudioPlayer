package ru.push.caudioplayer.core.facades.domain;

public enum PlaylistType {
	LOCAL, DEEZER;

	public String value() {
		return name();
	}

	public static PlaylistType fromValue(String v) {
		return valueOf(v);
	}
}
