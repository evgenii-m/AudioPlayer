package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.deezer.DeezerNeedAuthorizationException;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.domain.LastFmTrackData;
import ru.push.caudioplayer.core.playlist.domain.PlaylistType;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface MusicLibraryLogicFacade {

	void addEventListener(AudioPlayerEventListener listener);

	void removeEventListener(AudioPlayerEventListener listener);

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

	void createPlaylist(PlaylistType type);

	void deletePlaylist(String playlistUid);

	void renamePlaylist(String playlistUid, String newTitle);

	void backupPlaylists(String folderName);

	void addFilesToPlaylist(String playlistUid, List<File> files);

	void deleteItemsFromPlaylist(String playlistUid, List<Integer> itemsIndexes);

	void addLocationsToPlaylist(String playlistUid, List<String> locations);

	void addLastFmTrackDeezerPlaylist(String playlistUid, LastFmTrackData trackData);

	void addLastFmTrackToDeezerLovedTracks(LastFmTrackData trackData);
}
