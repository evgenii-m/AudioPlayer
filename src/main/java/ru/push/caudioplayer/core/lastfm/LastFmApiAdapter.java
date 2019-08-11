package ru.push.caudioplayer.core.lastfm;

public interface LastFmApiAdapter {

	String getUserAuthorizationPageUrl(String token);

	String authGetToken();

	String authGetSession(String token);
}
