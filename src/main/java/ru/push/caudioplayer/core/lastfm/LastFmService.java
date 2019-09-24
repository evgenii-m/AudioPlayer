package ru.push.caudioplayer.core.lastfm;

import ru.push.caudioplayer.core.lastfm.model.Track;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 5/12/17
 */
public interface LastFmService {

	String getToken();

	String getUserAuthorizationPageUrl(String token);

	boolean setSessionByToken(String token, String pageUrl);

	List<Track> getUserRecentTracks(boolean fetchMore);

	TrackInfo getTrackInfo(String artistName, String trackTitle);

  boolean updateNowPlaying(String artistName, String trackTitle, String albumName);

  long calculateScrobbleDelay(PlaylistTrack track);

  boolean scrobbleTrack(String artistName, String trackTitle, String albumName, Date timestamp, Boolean chosenByUser);
}
