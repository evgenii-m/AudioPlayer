package ru.push.caudioplayer.core.deezer;

import ru.push.caudioplayer.core.deezer.domain.Playlist;
import ru.push.caudioplayer.core.deezer.domain.Playlists;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.deezer.domain.Tracks;
import ru.push.caudioplayer.core.deezer.domain.internal.PlaylistId;

import java.util.List;

public interface DeezerApiAdapter {

	String getUserAuthorizationPageUrl();

	String getAccessToken(String code);

	Track getTrack(long trackId, String accessToken) throws DeezerApiErrorException;

	Playlists getPlaylists(String accessToken, Integer index, Integer limit) throws DeezerApiErrorException;

	Playlist getPlaylist(long playlistId, String accessToken) throws DeezerApiErrorException;

	Tracks getPlaylistTracks(long playlistId, String accessToken, Integer index, Integer limit) throws DeezerApiErrorException;

	PlaylistId createPlaylist(String title, String accessToken) throws DeezerApiErrorException;

	boolean deletePlaylist(long playlistId, String accessToken) throws DeezerApiErrorException;

	boolean renamePlaylist(long playlistId, String newTitle, String accessToken) throws DeezerApiErrorException;

	boolean addTracksToPlaylist(long playlistId, List<Long> trackIds, String accessToken) throws DeezerApiErrorException;

	boolean removeTracksFromPlaylist(long playlistId, List<Long> trackIds, String accessToken) throws DeezerApiErrorException;

	Tracks searchTracksQuery(String query, String accessToken) throws DeezerApiErrorException;

}
