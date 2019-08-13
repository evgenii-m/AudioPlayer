package ru.push.caudioplayer.core.lastfm;


public interface LastFmApiAdapter {

	String getUserAuthorizationPageUrl(String token);

	String authGetToken();

	LastFmSessionData authGetSession(String token);
}
