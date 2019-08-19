package ru.push.caudioplayer.core.deezer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.deezer.DeezerApiAdapter;
import ru.push.caudioplayer.core.deezer.DeezerApiConst;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.DeezerNeedAuthorizationException;
import ru.push.caudioplayer.core.deezer.domain.Playlist;
import ru.push.caudioplayer.core.deezer.domain.Playlists;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.services.AppConfigurationService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class DeezerApiServiceImpl implements DeezerApiService {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiServiceImpl.class);

	private static final Integer PLAYLISTS_DEFAULT_LIMIT = 100;

	@Autowired
	private Properties properties;

	@Autowired
	private DeezerApiAdapter deezerApiAdapter;

	@Autowired
	private AppConfigurationService appConfigurationService;

	private String currentAccessToken;

	public DeezerApiServiceImpl() {
	}

	@PostConstruct
	public void init() {
		String accessToken = appConfigurationService.getDeezerAccessToken();
		if (accessToken != null) {
			LOG.info("Load Deezer access token from configuration: {}", accessToken);
			this.currentAccessToken = accessToken;
		}
	}

	@Override
	public String getUserAuthorizationPageUrl() {
		return deezerApiAdapter.getUserAuthorizationPageUrl();
	}

	@Override
	public String checkAuthorizationCode(String locationUri) throws DeezerNeedAuthorizationException {
		assert locationUri != null;

		if (locationUri.startsWith(DeezerApiConst.DEEZER_API_DEFAULT_REDIRECT_URI)) {
			LOG.debug("redirect location url detected: {}", locationUri);

			int codeParamStartPosition = locationUri.indexOf(DeezerApiConst.DEEZER_API_AUTH_PARAM_CODE_NAME);
			if (codeParamStartPosition > 0) {
				// append param name length and 1 for character '=' to make substring only for value
				String deezerAppAuthCode = locationUri.substring(
						codeParamStartPosition + DeezerApiConst.DEEZER_API_AUTH_PARAM_CODE_NAME.length() + 1);
				LOG.info("Deezer authorization code: {}", deezerAppAuthCode);
				return deezerAppAuthCode;
			}

			int errorReasonParamStartPosition = locationUri.indexOf(DeezerApiConst.DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME);
			if (errorReasonParamStartPosition > 0) {
				String errorReason = locationUri.substring(
						errorReasonParamStartPosition + DeezerApiConst.DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME.length() + 1);
				throw new DeezerNeedAuthorizationException("Error Reason: " + errorReason);
			}
		}

		return null;
	}

	@Override
	public String getAccessToken(String code) {

		String accessToken = appConfigurationService.getDeezerAccessToken();
		if (accessToken != null) {
			LOG.warn("Deezer access token already set in configuration, they will be overwritten: access token = {}", accessToken);
		}

		String newAccessToken = deezerApiAdapter.getAccessToken(code);
		if (newAccessToken != null) {
			LOG.info("Set new Deezer access token: {}", newAccessToken);
			currentAccessToken = newAccessToken;
			appConfigurationService.saveDeezerAccessToken(currentAccessToken);
		} else {
			LOG.warn("Received empty access token, current user token will not be updated");
		}

		return currentAccessToken;
	}

	@Override
	public void getTrack(long trackId) throws DeezerNeedAuthorizationException {
		checkAccessToken();
		try {
			Track track = deezerApiAdapter.getTrack(trackId, currentAccessToken);
			LOG.debug("Received deezer track: {}", track);
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error:", e);
		}
	}

	@Override
	public void getPlaylists() throws DeezerNeedAuthorizationException {
		checkAccessToken();
		try {
			List<Playlist> playlists = new ArrayList<>();
			Playlists playlistsResponse;
			int index = 0;
			do {
				playlistsResponse = deezerApiAdapter.getPlaylists(currentAccessToken, index, PLAYLISTS_DEFAULT_LIMIT);
				playlists.addAll(playlistsResponse.getData());
				index += PLAYLISTS_DEFAULT_LIMIT;
			} while (playlistsResponse.getNext() != null);
			LOG.debug("Received deezer {} playlists: {}", playlists.size(), playlists);
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error:", e);
		}
	}

	private void checkAccessToken() throws DeezerNeedAuthorizationException {
		if (currentAccessToken == null) {
			throw new DeezerNeedAuthorizationException("Access token not defined.");
		}
	}

}
