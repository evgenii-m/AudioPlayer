package ru.push.caudioplayer.core.lastfm;

import org.apache.commons.lang3.tuple.Pair;

public interface LastFmApiAdapter {

	String getUserAuthorizationPageUrl(String token);

	String authGetToken();

	/**
	 * @return <username, session key>
	 */
	Pair<String, String> authGetSession(String token);
}
