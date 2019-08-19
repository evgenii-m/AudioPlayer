package ru.push.caudioplayer.core.deezer;

import ru.push.caudioplayer.core.deezer.domain.Playlists;
import ru.push.caudioplayer.core.deezer.domain.Track;

public interface DeezerApiAdapter {

	String getUserAuthorizationPageUrl();

	String getAccessToken(String code);

	Track getTrack(long trackId, String accessToken) throws DeezerApiErrorException;

	Playlists getPlaylists(String accessToken, Integer index, Integer limit) throws DeezerApiErrorException;
}
