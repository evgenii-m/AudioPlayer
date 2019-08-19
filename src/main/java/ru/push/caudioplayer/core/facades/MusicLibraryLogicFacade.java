package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.mediaplayer.domain.LastFmTrackData;

import java.util.List;
import java.util.function.Consumer;

public interface MusicLibraryLogicFacade {

	void connectLastFm(Consumer<String> openAuthPageConsumer);

	String getDeezerUserAuthorizationPageUrl();

	/**
	 * Method checks redirect URI from UI web browser component and if detect authorization code, make getToken request
	 * see https://developers.deezer.com/api/oauth
	 *
	 * @return true - terminate authorization
	 */
	boolean processDeezerAuthorization(String redirectUri);

	/**
	 * Return recent tracks list from Last.fm service for current user in chronological order.
	 * If user account not connected then return empty list.
	 */
	List<LastFmTrackData> getRecentTracksFromLastFm();

	void getTrackFromDeezer(int trackId);

	void getDeezerPlaylists();
}
