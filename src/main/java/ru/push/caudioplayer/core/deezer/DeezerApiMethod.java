package ru.push.caudioplayer.core.deezer;

public enum DeezerApiMethod {

	GET_TRACK("/track/%d"),
	USER_ME_PLAYLISTS("/user/me/playlists"),
	GET_PLAYLIST_TRACKS("/playlist/%d/tracks")
	;

	String value;

	DeezerApiMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "DeezerApiMethod{" +
				"value='" + value + '\'' +
				'}';
	}
}
