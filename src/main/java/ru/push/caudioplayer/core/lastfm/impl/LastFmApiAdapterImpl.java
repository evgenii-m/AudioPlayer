package ru.push.caudioplayer.core.lastfm.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.push.caudioplayer.core.lastfm.LastFmApiAdapter;
import ru.push.caudioplayer.core.lastfm.LastFmApiMethod;
import ru.push.caudioplayer.core.lastfm.LastFmApiParam;
import ru.push.caudioplayer.core.lastfm.LastFmSessionData;
import ru.push.caudioplayer.utils.StreamUtils;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * WARNING: this adapter is not thread safe
 */
@Component
public class LastFmApiAdapterImpl implements LastFmApiAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(LastFmApiAdapterImpl.class);

	private static final String LASTFM_API_AUTH_BASE_URL = "http://www.last.fm/api/auth/?api_key=%s&token=%s";
	private static final String LASTFM_API_SCHEME = "http";
	private static final String LASTFM_API_HOST = "ws.audioscrobbler.com/";
	private static final String LASTFM_API_VERSION = "2.0/";

	private final XPathFactory xPathFactory;
	private final URIBuilder baseApiUriBuilder;		// todo: think about make immutable builder
	private final HttpClient httpClient;

	@Autowired
	private Properties properties;

	private String lastfmApiKey;
	private String lastfmApiSharedSecret;

	@Value("${lastfm.api.request.retry.timeout.seconds}")
	private int retryTimeoutSeconds;
	@Value("${lastfm.api.request.getsession.retry.count}")
	private int getSessionRetryCount;

	public LastFmApiAdapterImpl() {
		this.xPathFactory = XPathFactory.newInstance();
		this.baseApiUriBuilder = new URIBuilder()
				.setScheme(LASTFM_API_SCHEME)
				.setHost(LASTFM_API_HOST)
				.setPath(LASTFM_API_VERSION);
		this.httpClient = HttpClientBuilder.create().build();
	}

	@PostConstruct
	public void init() {
		lastfmApiKey = properties.getProperty("lastfm.application.api.key");
		lastfmApiSharedSecret = properties.getProperty("lastfm.application.api.shared.secret");
	}

	@Override
	public String getUserAuthorizationPageUrl(String token) {
		return String.format(LASTFM_API_AUTH_BASE_URL, lastfmApiKey, token);
	}

	/**
	 * See https://www.last.fm/api/show/auth.getToken
	 */
	@Override
	public String authGetToken() {
		LastFmApiMethod method = LastFmApiMethod.AUTH_GET_TOKEN;
		Map<String, String> methodParameters = new HashMap<>();

		try {
			Document response = makeApiRequest(method, methodParameters);

			String expression = ".//*[local-name() = 'token']";
			String token = (String) xPathFactory.newXPath().evaluate(expression, response, XPathConstants.STRING);
			LOG.info("API token obtained: " + token);
			return token;
		} catch (XPathExpressionException e) {
			LOG.error("extract element from xml response error: ", e);
		}

		throw new IllegalStateException("Not satisfactory result from method " + method.getName());
	}

	/**
	 * See https://www.last.fm/api/show/auth.getSession
	 */
	@Override
	public LastFmSessionData authGetSession(String token) {
		LastFmApiMethod method = LastFmApiMethod.AUTH_GET_SESSION;
		Map<String, String> methodParameters = new HashMap<>();
		methodParameters.put(LastFmApiParam.TOKEN.getName(), token);

		try {
			Document response = makeApiRequest(method, methodParameters, getSessionRetryCount);

			String expression = ".//*[local-name() = 'name']";
			String username = (String) xPathFactory.newXPath().evaluate(expression, response, XPathConstants.STRING);
			LOG.info("session username: " + username);

			expression = ".//*[local-name() = 'key']";
			String sessionKey = (String) xPathFactory.newXPath().evaluate(expression, response, XPathConstants.STRING);
			LOG.info("session key: " + sessionKey);

			return new LastFmSessionData(username, sessionKey);
		} catch (XPathExpressionException e) {
			LOG.error("extract element from xml response error: ", e);
			throw new IllegalStateException("Not satisfactory result from method " + method.getName());
		}
	}

	private Document makeApiRequest(LastFmApiMethod method, Map<String, String> methodParameters) {
		return makeApiRequest(method, methodParameters, 0);
	}

	private Document makeApiRequest(LastFmApiMethod method, Map<String, String> methodParameters, int retryCount) {
		// append parameters required for all methods
		methodParameters.put(LastFmApiParam.METHOD_NAME.getName(), method.getName());
		methodParameters.put(LastFmApiParam.API_KEY.getName(), lastfmApiKey);

		// calculate signature for method parameters
		String methodSignature = getApiMethodSignature(methodParameters);
		methodParameters.put(LastFmApiParam.API_SIG.getName(), methodSignature);

		// make request
		baseApiUriBuilder.clearParameters();
		methodParameters.forEach(baseApiUriBuilder::addParameter);

		HttpGet request = null;
		try {
			URI apiUri = baseApiUriBuilder.build();

			// TODO: fix GUI application hangs by thread sleep
			for (; retryCount >= 0; retryCount--, Thread.sleep(retryTimeoutSeconds * 1000)) {
				request = new HttpGet(apiUri);
				try {
					LOG.info("api request: {}", request);
					HttpResponse response = httpClient.execute(request);
					LOG.info("api response: {}", response);
					// process response
					int statusCode = response.getStatusLine().getStatusCode();
					if (HttpStatus.SC_OK == statusCode) {
						String responseContent = StreamUtils.readStreamAsOneString(response.getEntity().getContent());
						LOG.info("response content: {}", responseContent);
						return XmlUtils.convertStringToXmlDocument(responseContent);
					}
				} finally {
					request.releaseConnection();
				}
			}

		} catch (URISyntaxException e) {
			LOG.error("construct uri error: ", e);
		} catch (SAXException | ParserConfigurationException e) {
			LOG.error("parsing xml from response error: ", e);
		} catch (IOException e) {
			LOG.error("http request error: request = {}, exception = {}", request, e);
		} catch (InterruptedException e) {
			LOG.error("timeout error: ", request, e);
		}

		throw new IllegalStateException("Not satisfactory result from method " + method.getName());

	}

	/**
	 * Signature calculated according to https://www.last.fm/api/desktopauth#6
	 */
	private String getApiMethodSignature(Map<String, String> parameters) {
		String signatureString = parameters.entrySet().stream()
				.sorted(Comparator.comparing(Map.Entry::getKey))
				.map(e -> e.getKey() + e.getValue())
				.collect(Collectors.joining());

		signatureString += lastfmApiSharedSecret;

		return DigestUtils.md5Hex(signatureString);
	}

}
