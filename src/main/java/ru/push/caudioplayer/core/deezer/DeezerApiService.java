package ru.push.caudioplayer.core.deezer;

public interface DeezerApiService {

	/**
	 * Return user authorization page URL as https://connect.deezer.com/oauth/auth.php with parameters
	 */
	String getUserAuthorizationPageUrl();

	/**
	 * Method for checking authorization code in location URI
	 *
	 * @return authorization code if detected
	 * @throws DeezerNeedAuthorizationException when error reason obtained
	 */
	String checkAuthorizationCode(String locationUri) throws DeezerNeedAuthorizationException;

	/**
	 * Get access token by received authorization code
	 *
	 * @return currentAccessToken (if receiving access token failed, previous access Token value will be returned)
	 */
	String getAccessToken(String code);

	/**
	 * Get track object
	 * See https://developers.deezer.com/api/track
	 */
	void getTrack(long trackId) throws DeezerNeedAuthorizationException;

	/**
	 * Get current user favorite playlists
	 * See https://developers.deezer.com/api/user/playlists
	 */
	void getPlaylists() throws DeezerNeedAuthorizationException;
}
