package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.deezer.DeezerNeedAuthorizationException;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.domain.LastFmTrackData;

import javax.xml.bind.JAXBException;
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

	void refreshPlaylists();

	List<PlaylistData> getPlaylists();

	PlaylistData getActivePlaylist();

	PlaylistData getDisplayedPlaylist();

	PlaylistData getPlaylist(String playlistUid);

	PlaylistData showPlaylist(String playlistUid);

	PlaylistData showActivePlaylist();

	PlaylistData createNewPlaylist();

	boolean deletePlaylist(String playlistUid);

	void renamePlaylist(String playlistUid, String newPlaylistName);

	void exportPlaylistToFile(String playlistUid, String folderPath) throws JAXBException;

	void backupPlaylists(String folderName);

	void addFilesToPlaylist(List<File> files);

	void deleteItemsFromPlaylist(List<Integer> itemsIndexes);

	void addLocationsToPlaylist(List<String> locations);

	void getTrackFromDeezer(int trackId) throws DeezerNeedAuthorizationException;

	List<PlaylistData> getDeezerPlaylists() throws DeezerNeedAuthorizationException;
}
