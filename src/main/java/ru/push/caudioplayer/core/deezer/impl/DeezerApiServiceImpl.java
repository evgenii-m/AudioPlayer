package ru.push.caudioplayer.core.deezer.impl;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.services.AppConfigurationService;
import ru.push.caudioplayer.utils.StreamUtils;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.Properties;

@Component
public class DeezerApiServiceImpl implements DeezerApiService {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiServiceImpl.class);

	private static final String DEEZER_API_AUTH_BASE_URL = "https://connect.deezer.com/oauth/auth.php" +
			"?app_id=%s&redirect_uri=%s&perms=%s";
	private static final String DEEZER_API_DEFAULT_REDIRECT_URI = "http://localhost:8888";
	private static final String DEEZER_API_AUTH_PARAM_CODE_NAME = "code";
	private static final String DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME = "error_reason";
	private static final String DEEZER_API_DEFAULT_PERMISSIONS = "basic_access,offline_access,manage_library,delete_library,listening_history";
	private static final String DEEZER_API_ACCESS_TOKEN_BASE_URL = "https://connect.deezer.com/oauth/access_token.php" +
			"?app_id=%s&secret=%s&code=%s&output=xml";

	@Autowired
	private Properties properties;

	@Autowired
	private AppConfigurationService appConfigurationService;

	private final XPathFactory xPathFactory;
	private final HttpClient httpClient;

	private String deezerAppId;
	private String deezerAppSecretKey;

	public DeezerApiServiceImpl() {
		this.xPathFactory = XPathFactory.newInstance();
		this.httpClient = HttpClientBuilder.create().build();
	}

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
	public String checkAuthorizationCode(String locationUri) throws IllegalAccessException {
		assert locationUri != null;

		if (locationUri.startsWith(DEEZER_API_DEFAULT_REDIRECT_URI)) {
			LOG.debug("redirect location url detected: {}", locationUri);

			int codeParamStartPosition = locationUri.indexOf(DEEZER_API_AUTH_PARAM_CODE_NAME);
			if (codeParamStartPosition > 0) {
				// append param name length and 1 for character '=' to make substring only for value
				String deezerAppAuthCode = locationUri.substring(codeParamStartPosition + DEEZER_API_AUTH_PARAM_CODE_NAME.length() + 1);
				LOG.info("Deezer authorization code: {}", deezerAppAuthCode);
				return deezerAppAuthCode;
			}

			int errorReasonParamStartPosition = locationUri.indexOf(DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME);
			if (errorReasonParamStartPosition > 0) {
				String errorReason = locationUri.substring(errorReasonParamStartPosition + DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME.length() + 1);
				throw new IllegalAccessException("Error Reason: " + errorReason);
			}
		}

		return null;
	}

	@Override
	public String getAccessToken(String code) {
		String accessTokenRequestUrl = String.format(DEEZER_API_ACCESS_TOKEN_BASE_URL, deezerAppId, deezerAppSecretKey, code);

		HttpGet request = new HttpGet(accessTokenRequestUrl);
		LOG.info("api request: {}", request);

		try {
			HttpResponse response = httpClient.execute(request);
			LOG.info("api response: {}", response);
			// process response
			int statusCode = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK == statusCode) {
				String responseContent = StreamUtils.readStreamAsOneString(response.getEntity().getContent());
				LOG.info("response content: {}", responseContent);

				Document responseDocument = XmlUtils.convertStringToXmlDocument(responseContent);
				String expression = ".//*[local-name() = 'access_token']";
				String accessToken = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
				return accessToken;
			}
		} catch (IOException e) {
			LOG.error("http request error: request = {}, exception = {}", request, e);
		} catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
			LOG.error("parsing xml from response error: ", e);
		} finally {
			request.releaseConnection();
		}

		return null;
	}

}
