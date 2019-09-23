package ru.push.caudioplayer.core.lastfm.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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
import ru.push.caudioplayer.core.lastfm.model.LastFmResponse;
import ru.push.caudioplayer.core.lastfm.model.ScrobblesResult;
import ru.push.caudioplayer.core.lastfm.model.UpdateNowPlayingResult;
import ru.push.caudioplayer.core.lastfm.model.RecentTracks;
import ru.push.caudioplayer.core.lastfm.model.TrackInfo;
import ru.push.caudioplayer.utils.StreamUtils;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * WARNING: this adapter is not thread safe
 */
@Component
public class LastFmApiAdapterImpl implements LastFmApiAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(LastFmApiAdapterImpl.class);

	private static final String LASTFM_API_AUTH_BASE_URL = "https://www.last.fm/api/auth?api_key=%s&token=%s";
	private static final String LASTFM_API_SCHEME = "https";
	private static final String LASTFM_API_HOST = "ws.audioscrobbler.com/";
	private static final String LASTFM_API_VERSION = "2.0/";
	private static final String VALID_PAGE_URL_PATTERN_FOR_GET_SESSION = LASTFM_API_AUTH_BASE_URL;
	private static final String LASTFM_RESPONSE_STATUS_OK = "ok";

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

	@Override
	public boolean validatePageUrlForGetSession(String token, String pageUrl) {
		String validPageUrlForGetSession = String.format(VALID_PAGE_URL_PATTERN_FOR_GET_SESSION, lastfmApiKey, token);
		return validPageUrlForGetSession.equals(pageUrl);
	}

	/**
	 * See https://www.last.fm/api/show/auth.getToken
	 */
	@Override
	public String authGetToken() {
		LastFmApiMethod method = LastFmApiMethod.AUTH_GET_TOKEN;
		Map<String, String> methodParameters = new HashMap<>();

		try {
			String response = makeApiGetRequest(method, methodParameters);
			if (response != null) {
				Document responseDocument = XmlUtils.convertStringToXmlDocument(response);

				String expression = ".//*[local-name() = 'token']";
				String token = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
				LOG.info("API token obtained: " + token);
				return token;
			}
		} catch (IOException| SAXException | ParserConfigurationException e) {
			LOG.error("parsing xml from response error: ", e);
		} catch (XPathExpressionException e) {
			LOG.error("extract element from xml response error: ", e);
		}

		return null;
	}

	/**
	 * See https://www.last.fm/api/show/auth.getSession
	 */
	@Override
	public Optional<LastFmSessionData> authGetSession(String token) {
		LastFmApiMethod method = LastFmApiMethod.AUTH_GET_SESSION;
		Map<String, String> methodParameters = new HashMap<>();
		methodParameters.put(LastFmApiParam.TOKEN.getName(), token);

		try {
			String response = makeApiGetRequest(method, methodParameters, getSessionRetryCount);
			if (response != null) {
				Document responseDocument = XmlUtils.convertStringToXmlDocument(response);

				String expression = ".//*[local-name() = 'name']";
				String username = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
				LOG.info("session username: " + username);

				expression = ".//*[local-name() = 'key']";
				String sessionKey = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
				LOG.info("session key: " + sessionKey);

				return Optional.of(new LastFmSessionData(username, sessionKey));
			}
		} catch (IOException| SAXException | ParserConfigurationException e) {
			LOG.error("parsing xml from response error: ", e);
		} catch (XPathExpressionException e) {
			LOG.error("extract element from xml response error: ", e);
		}

		return Optional.empty();
	}

	/**
	 * See https://www.last.fm/api/show/user.getRecentTracks
	 */
	@Override
	public Optional<RecentTracks> userGetRecentTracks(Integer limit, @NotNull String username, Integer page,
																										Date from, Boolean extended, Date to) {
		LastFmApiMethod method = LastFmApiMethod.USER_GET_RECENT_TRACKS;
		Map<String, String> methodParameters = new HashMap<>();
		methodParameters.put(LastFmApiParam.USER.getName(), username);
		if (limit != null) {
			methodParameters.put(LastFmApiParam.LIMIT.getName(), limit.toString());
		}
		if (page != null) {
			methodParameters.put(LastFmApiParam.PAGE.getName(), String.valueOf(page));
		}
		if (from != null) {
			methodParameters.put(LastFmApiParam.FROM.getName(), String.valueOf(from.getTime()));
		}
		if (extended != null) {
			methodParameters.put(LastFmApiParam.EXTENDED.getName(), extended ? "1" : "0");
		}
		if (to != null) {
			methodParameters.put(LastFmApiParam.TO.getName(), String.valueOf(to.getTime()));
		}

		try {
			String response = makeApiGetRequest(method, methodParameters);
			if (response != null) {
				LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
				if (validateLastFmResponse(lastFmResponse)) {
					RecentTracks recentTracks = lastFmResponse.getRecentTracks();
					LOG.debug("obtained recent tracks: {}", recentTracks);
					return Optional.ofNullable(recentTracks);
				}
			}
		} catch (JAXBException e) {
			LOG.error("parsing xml from response error: ", e);
		}

		return Optional.empty();
	}

	@Override
	public Optional<TrackInfo> getTrackInfo(String mbid, String track, String artist, String username, Boolean autocorrect) {
		LastFmApiMethod method = LastFmApiMethod.TRACK_GET_INFO;
		Map<String, String> methodParameters = new HashMap<>();
		if (mbid != null) {
			methodParameters.put(LastFmApiParam.MBID.getName(), mbid);
		}
		if (track != null) {
			methodParameters.put(LastFmApiParam.TRACK.getName(), track);
		}
		if (artist != null) {
			methodParameters.put(LastFmApiParam.ARTIST.getName(), artist);
		}
		if (username != null) {
			methodParameters.put(LastFmApiParam.USERNAME.getName(), username);
		}
		if (autocorrect != null) {
			methodParameters.put(LastFmApiParam.AUTOCORRECT.getName(), autocorrect ? "1" : "0");
		}

		try {
			String response = makeApiGetRequest(method, methodParameters);
			if (response != null) {
				LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
				if (validateLastFmResponse(lastFmResponse)) {
					TrackInfo trackInfo = lastFmResponse.getTrackInfo();
					LOG.debug("obtained track info: {}", trackInfo);
					return Optional.ofNullable(trackInfo);
				}
			}
		} catch (JAXBException e) {
			LOG.error("parsing xml from response error: ", e);
		}

		return Optional.empty();
	}

	@Override
	public Optional<UpdateNowPlayingResult> updateNowPlaying(@NotNull String sessionKey, @NotNull String artist,
																													 @NotNull String track, String album, Long duration) {
		LastFmApiMethod method = LastFmApiMethod.TRACK_UPDATE_NOW_PLAYING;
		Map<String, String> methodParameters = new HashMap<>();
		methodParameters.put(LastFmApiParam.SESSION_KEY.getName(), sessionKey);
		methodParameters.put(LastFmApiParam.ARTIST.getName(), artist);
		methodParameters.put(LastFmApiParam.TRACK.getName(), track);
		if (album != null) {
			methodParameters.put(LastFmApiParam.ALBUM.getName(), album);
		}
		if (duration != null) {
			methodParameters.put(LastFmApiParam.DURATION.getName(), String.valueOf(duration));
		}

		try {
			String response = makeApiPostRequest(method, methodParameters);
			if (response != null) {
				LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
				if (validateLastFmResponse(lastFmResponse)) {
					UpdateNowPlayingResult updateNowPlayingResult = lastFmResponse.getUpdateNowPlayingResult();
					LOG.debug("obtained updating now playing result: {}", updateNowPlayingResult);
					return Optional.ofNullable(updateNowPlayingResult);
				}
			}
		} catch (JAXBException e) {
			LOG.error("parsing xml from response error: ", e);
		}

		return Optional.empty();
	}

	@Override
	public Optional<ScrobblesResult> scrobbleTrack(@NotNull String sessionKey, @NotNull String artist, @NotNull String track,
																								 int timestamp, String album, Boolean chosenByUser, Long duration) {
		LastFmApiMethod method = LastFmApiMethod.TRACK_SCROBBLE;
		Map<String, String> methodParameters = new HashMap<>();
		methodParameters.put(LastFmApiParam.SESSION_KEY.getName(), sessionKey);
		methodParameters.put(LastFmApiParam.ARTIST.getName(), artist);
		methodParameters.put(LastFmApiParam.TRACK.getName(), track);
		methodParameters.put(LastFmApiParam.TIMESTAMP.getName(), String.valueOf(timestamp));
		if (album != null) {
			methodParameters.put(LastFmApiParam.ALBUM.getName(), album);
		}
		if (chosenByUser != null) {
			methodParameters.put(LastFmApiParam.CHOSEN_BY_USER.getName(), chosenByUser ? "1" : "0");
		}
		if (duration != null) {
			methodParameters.put(LastFmApiParam.DURATION.getName(), String.valueOf(duration));
		}

		try {
			String response = makeApiPostRequest(method, methodParameters);
			if (response != null) {
				LastFmResponse lastFmResponse = XmlUtils.unmarshalDocumnet(response, LastFmResponse.class.getPackage().getName());
				if (validateLastFmResponse(lastFmResponse)) {
					ScrobblesResult scrobblesResult = lastFmResponse.getScrobblesResult();
					LOG.debug("obtained scrobbles result: {}", scrobblesResult);
					return Optional.ofNullable(scrobblesResult);
				}
			}
		} catch (JAXBException e) {
			LOG.error("parsing xml from response error: ", e);
		}

		return Optional.empty();
	}

	private String makeApiGetRequest(LastFmApiMethod method, Map<String, String> methodParameters) {
		return makeApiRequest(method, methodParameters, 0, HttpGet::new);
	}

	private String makeApiGetRequest(LastFmApiMethod method, Map<String, String> methodParameters, int retryCount) {
		return makeApiRequest(method, methodParameters, retryCount, HttpGet::new);
	}

	private String makeApiPostRequest(LastFmApiMethod method, Map<String, String> methodParameters) {
		return makeApiRequest(method, methodParameters, 0, HttpPost::new);
	}

	private String makeApiPostRequest(LastFmApiMethod method, Map<String, String> methodParameters, int retryCount) {
		return makeApiRequest(method, methodParameters, retryCount, HttpPost::new);
	}

	private <T extends HttpRequestBase> String makeApiRequest(LastFmApiMethod method, Map<String, String> methodParameters,
																														int retryCount, Function<URI, T> constructRequestFunction) {
		// append parameters required for all methods
		methodParameters.put(LastFmApiParam.METHOD_NAME.getName(), method.getName());
		methodParameters.put(LastFmApiParam.API_KEY.getName(), lastfmApiKey);

		// calculate signature for method parameters
		String methodSignature = getApiMethodSignature(methodParameters);
		methodParameters.put(LastFmApiParam.API_SIG.getName(), methodSignature);

		// make request
		baseApiUriBuilder.clearParameters();
		methodParameters.forEach(baseApiUriBuilder::addParameter);

		T request = null;
		try {
			URI apiUri = baseApiUriBuilder.build();

			// TODO: make request in other thread, not to delay UI
			for (; retryCount >= 0; retryCount--, Thread.sleep(retryTimeoutSeconds * 1000)) {
				request = constructRequestFunction.apply(apiUri);
				try {
					LOG.info("api request: {}", request);
					HttpResponse response = httpClient.execute(request);
					LOG.info("api response: {}", response);
					// process response
					int statusCode = response.getStatusLine().getStatusCode();
					if (HttpStatus.SC_OK == statusCode) {
						String responseContent = StreamUtils.readStreamAsOneString(response.getEntity().getContent());
						LOG.debug("response content: {}", responseContent);
						return responseContent;
					}
				} finally {
					request.releaseConnection();
				}
			}

		} catch (URISyntaxException e) {
			LOG.error("construct uri error: ", e);
		} catch (IOException e) {
			LOG.error("http request error: request = {}, exception = {}", request, e);
		} catch (InterruptedException e) {
			LOG.error("timeout error: ", request, e);
		}

		return null;
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

	private boolean validateLastFmResponse(LastFmResponse lastFmResponse) {
		if ((lastFmResponse != null) && (LASTFM_RESPONSE_STATUS_OK.equals(lastFmResponse.getStatus()))) {
			return true;
		} else {
			LOG.error("Invalid LastFmResponse status: response = {}", lastFmResponse);
			return false;
		}
	}

}
