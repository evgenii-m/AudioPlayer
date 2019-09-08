package ru.push.caudioplayer.core.playlist.model;

public enum PlaylistType {
	LOCAL, DEEZER;

	public String value() {
		return name();
	}

	public static PlaylistType fromValue(String v) {
		return valueOf(v);
	}
}
