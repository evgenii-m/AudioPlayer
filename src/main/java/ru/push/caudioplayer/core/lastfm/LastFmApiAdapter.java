package ru.push.caudioplayer.core.lastfm;


import ru.push.caudioplayer.core.lastfm.model.RecentTracks;

import java.util.Date;
import java.util.Optional;

public interface LastFmApiAdapter {

	String getUserAuthorizationPageUrl(String token);

	boolean validatePageUrlForGetSession(String token, String pageUrl);

	String authGetToken();

	Optional<LastFmSessionData> authGetSession(String token);

	Optional<RecentTracks> userGetRecentTracks(Integer limit, String username, Integer page,
																						 Date from, Boolean extended, Date to);
}
