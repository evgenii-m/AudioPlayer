package ru.push.caudioplayer.core.lastfm;

public enum LastFmApiMethod {

	AUTH_GET_TOKEN("auth.getToken"),
	AUTH_GET_SESSION("auth.getSession");

	private String name;

	LastFmApiMethod(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
