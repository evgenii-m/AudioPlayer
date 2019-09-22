package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.facades.dto.LastFmTrackInfoData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface MusicLibraryLogicFacade {

	void addEventListener(AudioPlayerEventListener listener);

	void removeEventListener(AudioPlayerEventListener listener);

	String getLastFmToken();

	String getLastFmAuthorizationPageUrl(String token);

	boolean processLastFmAuthorization(String token, String pageUrl);

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
	List<LastFmTrackData> getRecentTracksFromLastFm(boolean fetchMore);

	LastFmTrackInfoData getLastFmTrackInfo(LastFmTrackData trackData);

	void reloadPlaylists();

	List<PlaylistData> getLocalPlaylists();

	List<PlaylistData> getDeezerPlaylists();

	PlaylistData getActivePlaylist();

	String getDeezerPlaylistWebPageUrl(String playlistUid);

	void createLocalPlaylist();

	void createDeezerPlaylist();

	void deletePlaylist(String playlistUid);

	void renamePlaylist(String playlistUid, String newTitle);

	void backupPlaylists(String folderName);

	void exportPlaylistToFile(String playlistUid, String folderPath);

	void addFilesToPlaylist(String playlistUid, List<File> files);

	void deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid);

	void addLocationsToPlaylist(String playlistUid, List<String> locations);

	void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackData trackData);

	void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(LastFmTrackData trackData);

	void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackInfoData trackInfoData);

	void addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(LastFmTrackInfoData trackInfoData);
}
