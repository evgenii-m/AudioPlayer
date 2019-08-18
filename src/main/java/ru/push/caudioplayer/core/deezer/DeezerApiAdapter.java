package ru.push.caudioplayer.core.deezer;

public interface DeezerApiAdapter {

	String getUserAuthorizationPageUrl();

	String getAccessToken(String code);

	void getTrack(long trackId, String accessToken);
}
