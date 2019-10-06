package ru.push.caudioplayer.core.lastfm;


import ru.push.caudioplayer.core.lastfm.model.ScrobblesResult;
import ru.push.caudioplayer.core.lastfm.model.UpdateNowPlayingResult;
import ru.push.caudioplayer.core.lastfm.model.RecentTracks;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Optional;

public interface LastFmApiProvider {

	String getUserAuthorizationPageUrl(String token);

	boolean validatePageUrlForGetSession(String token, String pageUrl);

	String authGetToken();

	Optional<LastFmSessionData> authGetSession(String token);

	Optional<RecentTracks> userGetRecentTracks(Integer limit, @NotNull String username, Integer page,
																						 Date from, Boolean extended, Date to);

	Optional<TrackInfo> getTrackInfo(String mbid, String track, String artist, String username, Boolean autocorrect);

	Optional<UpdateNowPlayingResult> updateNowPlaying(@NotNull String sessionKey, @NotNull String artist,
																										@NotNull String track, String album, Long duration);

	Optional<ScrobblesResult> scrobbleTrack(@NotNull String sessionKey, @NotNull String artist, @NotNull String track,
																					int timestamp, String album, Boolean chosenByUser, Long duration);
}
