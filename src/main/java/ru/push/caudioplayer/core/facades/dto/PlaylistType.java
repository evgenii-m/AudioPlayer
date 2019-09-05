package ru.push.caudioplayer.core.facades.dto;

public enum PlaylistType {
	LOCAL, DEEZER;

	public String value() {
		return name();
	}

	public static PlaylistType fromValue(String v) {
		return valueOf(v);
	}
}
