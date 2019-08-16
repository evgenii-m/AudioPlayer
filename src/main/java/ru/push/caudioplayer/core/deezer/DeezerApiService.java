package ru.push.caudioplayer.core.deezer;

public interface DeezerApiService {

	String getUserAuthorizationPageUrl();

	String getAccessTokenPageUrl(String code);
}
