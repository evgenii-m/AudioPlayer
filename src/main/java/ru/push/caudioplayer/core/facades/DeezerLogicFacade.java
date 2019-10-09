package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackInfoData;

public interface DeezerLogicFacade extends PlaylistLogicFacade {

	String getDeezerUserAuthorizationPageUrl();

	/**
	 * Method checks redirect URI from UI web browser component and if detect authorization code, make getToken request
	 * see https://developers.deezer.com/api/oauth
	 *
	 * @return true - terminate authorization
	 */
	boolean processDeezerAuthorization(String redirectUri);

	String getDeezerPlaylistWebPageUrl(String playlistUid);

	void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackData trackData);

	void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(LastFmTrackData trackData);

	void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackInfoData trackInfoData);

	void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(LastFmTrackInfoData trackInfoData);
}
