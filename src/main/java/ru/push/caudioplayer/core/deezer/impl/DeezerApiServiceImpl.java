package ru.push.caudioplayer.core.deezer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.deezer.DeezerApiService;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class DeezerApiServiceImpl implements DeezerApiService {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiServiceImpl.class);

	private static final String DEEZER_API_AUTH_BASE_URL = "https://connect.deezer.com/oauth/auth.php" +
			"?app_id=%s&redirect_uri=%s&perms=%s";
	private static final String DEEZER_API_DEFAULT_REDIRECT_URI = "http://localhost:8888";
	private static final String DEEZER_API_DEFAULT_PERMISSIONS = "basic_access,offline_access,manage_library,delete_library,listening_history";
	private static final String DEEZER_API_ACCESS_TOKEN_BASE_URL = "https://connect.deezer.com/oauth/access_token.php" +
			"?app_id=%s&secret=%s&code=%s&output=xml";


	@Autowired
	private Properties properties;

	private String deezerAppId;
	private String deezerAppSecretKey;

	@PostConstruct
	public void init() {
		deezerAppId = properties.getProperty("deezer.application.api.id");
		deezerAppSecretKey = properties.getProperty("deezer.application.api.secret.key");
	}

	@Override
	public String getUserAuthorizationPageUrl() {
		return String.format(DEEZER_API_AUTH_BASE_URL, deezerAppId, DEEZER_API_DEFAULT_REDIRECT_URI, DEEZER_API_DEFAULT_PERMISSIONS);
	}

	@Override
	public String getAccessTokenPageUrl(String code) {
		return String.format(DEEZER_API_ACCESS_TOKEN_BASE_URL, deezerAppId, deezerAppSecretKey, code);
	}

}
