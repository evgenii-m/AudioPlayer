package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.facades.dto.LastFmTrackInfoData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface MusicLibraryLogicFacade extends PlaylistLogicFacade {

	String getLastFmToken();

	String getLastFmAuthorizationPageUrl(String token);

	boolean processLastFmAuthorization(String token, String pageUrl);

	/**
	 * Return recent tracks list from Last.fm service for current user in chronological order.
	 * If user account not connected then return empty list.
	 */
	List<LastFmTrackData> getRecentTracksFromLastFm(boolean fetchMore);

	LastFmTrackInfoData getLastFmTrackInfo(LastFmTrackData trackData);

	void addFilesToPlaylist(String playlistUid, List<File> files);

	void addLocationsToPlaylist(String playlistUid, List<String> locations);
}
