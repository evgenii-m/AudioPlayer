package ru.push.caudioplayer.core.deezer;

public enum DeezerApiMethod {

	GET_TRACK("/track/%d", DeezerApiRequestMethodType.GET),
	GET_PLAYLIST("/playlist/%d", DeezerApiRequestMethodType.GET),
	GET_PLAYLIST_TRACKS("/playlist/%d/tracks", DeezerApiRequestMethodType.GET),
	DELETE_USER_PLAYLIST("/playlist/%d", DeezerApiRequestMethodType.DELETE),
	UPDATE_USER_PLAYLIST("/playlist/%d", DeezerApiRequestMethodType.POST),
	PLAYLIST_ADD_TRACK("/playlist/%s/tracks", DeezerApiRequestMethodType.POST),
	PLAYLIST_REMOVE_TRACK("/playlist/%s/tracks", DeezerApiRequestMethodType.DELETE),
	SEARCH_TRACK_QUERY("/search/track", DeezerApiRequestMethodType.GET),

	USER_PLAYLISTS("/user/me/playlists", DeezerApiRequestMethodType.GET),
	USER_PLAYLIST_CREATE("/user/me/playlists", DeezerApiRequestMethodType.POST),
	USER_FAVORITES_ADD_TRACK("/user/me/tracks", DeezerApiRequestMethodType.POST),
	USER_FAVORITES_REMOVE_TRACK("/user/me/tracks", DeezerApiRequestMethodType.DELETE)
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

	public String formate(Object... args) {
		return String.format(value, args);
	}

	@Override
	public String toString() {
		return "DeezerApiMethod{" +
				"value='" + value + '\'' +
				", methodType=" + methodType +
				'}';
	}
}
