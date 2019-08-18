package ru.push.caudioplayer.core.deezer;

public interface DeezerApiConst {

	String DEEZER_API_DEFAULT_REDIRECT_URI = "http://localhost:8888";
	String DEEZER_API_AUTH_BASE_URL = "https://connect.deezer.com/oauth/auth.php" +
			"?app_id=%s&redirect_uri=%s&perms=%s";
	String DEEZER_API_ACCESS_TOKEN_BASE_URL = "https://connect.deezer.com/oauth/access_token.php" +
			"?app_id=%s&secret=%s&code=%s&output=xml";
	String DEEZER_API_DEFAULT_PERMISSIONS = "basic_access,offline_access,manage_library,delete_library,listening_history";
	String DEEZER_API_AUTH_PARAM_CODE_NAME = "code";
	String DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME = "error_reason";

	String DEEZER_API_SCHEME = "https";
	String DEEZER_API_HOST = "api.deezer.com";
}
