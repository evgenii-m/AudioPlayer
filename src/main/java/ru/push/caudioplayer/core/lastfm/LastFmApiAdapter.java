package ru.push.caudioplayer.core.lastfm;


import ru.push.caudioplayer.core.lastfm.domain.RecentTracks;

import java.util.Date;

public interface LastFmApiAdapter {

	String getUserAuthorizationPageUrl(String token);

	String authGetToken();

	LastFmSessionData authGetSession(String token);

	RecentTracks userGetRecentTracks(Integer limit, String username, Integer page, Date from, Boolean extended, Date to);
}
