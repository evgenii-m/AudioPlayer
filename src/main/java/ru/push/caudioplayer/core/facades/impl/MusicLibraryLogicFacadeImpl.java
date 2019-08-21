package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.DeezerNeedAuthorizationException;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.lastfm.LastFmService;
import ru.push.caudioplayer.core.lastfm.domain.Track;
import ru.push.caudioplayer.core.mediaplayer.domain.LastFmTrackData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class MusicLibraryLogicFacadeImpl implements MusicLibraryLogicFacade {

	private static final Logger LOG = LoggerFactory.getLogger(MusicLibraryLogicFacadeImpl.class);

	@Autowired
	private LastFmService lastFmService;
	@Autowired
	private DeezerApiService deezerApiService;
	@Autowired
	private ApplicationConfigService applicationConfigService;


	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		try {
			if (applicationConfigService.getDeezerAccessToken() != null) {
				getDeezerPlaylists();
			}
		} catch (DeezerNeedAuthorizationException e) {
			LOG.error("Deezer authorization fails: {}", e);
		}
	}

	@Override
	public void connectLastFm(Consumer<String> openAuthPageConsumer) {
		lastFmService.connectLastFm(openAuthPageConsumer);
	}

	@Override
	public String getDeezerUserAuthorizationPageUrl() {
		return deezerApiService.getUserAuthorizationPageUrl();
	}

	@Override
	public boolean processDeezerAuthorization(String redirectUri) {
		try {
			String authorizationCode = deezerApiService.checkAuthorizationCode(redirectUri);
			if (authorizationCode != null) {
				// request for access token by received authorization code
				String accessToken = deezerApiService.getAccessToken(authorizationCode);
				if (accessToken != null) {
					LOG.info("Deezer access token: {}", accessToken);
					// if access token received - authorization process ends
					return true;
				} else {
					LOG.error("Deezer access token is NULL");
				}
			}
		} catch (DeezerNeedAuthorizationException e) {
			LOG.error("Deezer authorization fails: {}", e);
			// if access error received - authorization process also ends
			return true;
		}

		// return false for continue checking
		return false;
	}

	@Override
	public List<LastFmTrackData> getRecentTracksFromLastFm() {
		List<Track> userRecentTracks = lastFmService.getUserRecentTracks();

		if (CollectionUtils.isEmpty(userRecentTracks)) {
			return new ArrayList<>();
		}

		return userRecentTracks.stream()
				.map(o -> new LastFmTrackData(o.getArtist().getName(), o.getAlbum().getName(), o.getName(), o.getNowPlaying(),
						((o.getDate() != null) && (o.getDate().getUts() != null)) ? new Date(o.getDate().getUts() * 1000) : null))
				.sorted((o1, o2) -> (o2.getScrobbleDate() != null) ? o2.getScrobbleDate().compareTo(o1.getScrobbleDate()) : 1)
				.collect(Collectors.toList());
	}

	@Override
	public void getTrackFromDeezer(int trackId) throws DeezerNeedAuthorizationException {
		deezerApiService.getTrack(trackId);
	}

	@Override
	public void getDeezerPlaylists() throws DeezerNeedAuthorizationException {

		List<PlaylistData> playlists = deezerApiService.getPlaylists();

	}
}
