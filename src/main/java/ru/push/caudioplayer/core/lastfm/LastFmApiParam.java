package ru.push.caudioplayer.core.lastfm;

public enum LastFmApiParam {

	API_KEY("api_key"),
	API_SIG("api_sig"),
	METHOD_NAME("method"),
	TOKEN("token"),
	SESSION_KEY("sk"),
	LIMIT("limit"),
	USER("user"),
	PAGE("page"),
	FROM("from"),
	EXTENDED("extended"),
	TO("to"),
	MBID("mbid"),
	TRACK("track"),
	ARTIST("artist"),
	ALBUM("album"),
	DURATION("duration"),
	USERNAME("username"),
	AUTOCORRECT("autocorrect"),
	TIMESTAMP("timestamp"),
	CHOSEN_BY_USER("chosenByUser"),
	;

	private String name;

	LastFmApiParam(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
