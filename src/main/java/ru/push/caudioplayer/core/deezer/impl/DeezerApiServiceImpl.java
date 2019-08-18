package ru.push.caudioplayer.core.deezer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.deezer.DeezerApiAdapter;
import ru.push.caudioplayer.core.deezer.DeezerApiConst;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.services.AppConfigurationService;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class DeezerApiServiceImpl implements DeezerApiService {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiServiceImpl.class);


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
	public String checkAuthorizationCode(String locationUri) throws IllegalAccessException {
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
				throw new IllegalAccessException("Error Reason: " + errorReason);
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
	public void getTrack(long trackId) {

	}

}
