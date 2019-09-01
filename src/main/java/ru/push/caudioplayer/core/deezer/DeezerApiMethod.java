package ru.push.caudioplayer.core.deezer;

public enum DeezerApiMethod {

	GET_TRACK("/track/%d", DeezerApiRequestMethodType.GET),
	USER_ME_PLAYLISTS("/user/me/playlists", DeezerApiRequestMethodType.GET),
	GET_PLAYLIST("/playlist/%d", DeezerApiRequestMethodType.GET),
	GET_PLAYLIST_TRACKS("/playlist/%d/tracks", DeezerApiRequestMethodType.GET),
	CREATE_USER_PLAYLIST("/user/me/playlists", DeezerApiRequestMethodType.POST),
	DELETE_USER_PLAYLIST("/playlist/%d", DeezerApiRequestMethodType.DELETE),
	UPDATE_USER_PLAYLIST("/playlist/%d", DeezerApiRequestMethodType.POST)
	;

	String value;
	DeezerApiRequestMethodType methodType;

	DeezerApiMethod(String value, DeezerApiRequestMethodType methodType) {
		this.value = value;
		this.methodType = methodType;
	}

	public String getValue() {
		return value;
	}

	public DeezerApiRequestMethodType getMethodType() {
		return methodType;
	}

	@Override
	public String toString() {
		return "DeezerApiMethod{" +
				"value='" + value + '\'' +
				", methodType=" + methodType +
				'}';
	}
}
