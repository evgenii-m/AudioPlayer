package ru.push.caudioplayer.core.lastfm;

public enum LastFmApiParam {

	API_KEY("api_key"),
	API_SIG("api_sig"),
	METHOD_NAME("method"),
	TOKEN("token"),
	LIMIT("limit"),
	USER("user"),
	PAGE("page"),
	FROM("from"),
	EXTENDED("extended"),
	TO("to"),
	MBID("mbid"),
	TRACK("track"),
	ARTIST("artist"),
	USERNAME("username"),
	AUTOCORRECT("autocorrect"),
	;

	private String name;

	LastFmApiParam(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
