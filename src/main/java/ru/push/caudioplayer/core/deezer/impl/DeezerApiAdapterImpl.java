package ru.push.caudioplayer.core.deezer.impl;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ru.push.caudioplayer.core.deezer.DeezerApiAdapter;
import ru.push.caudioplayer.core.deezer.DeezerApiConst;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiMethod;
import ru.push.caudioplayer.core.deezer.DeezerApiParam;
import ru.push.caudioplayer.core.deezer.domain.Playlists;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.deezer.domain.Tracks;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ThreadSafe
@Component
public class DeezerApiAdapterImpl implements DeezerApiAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiAdapterImpl.class);

	private final CloseableHttpClient httpClient;

	@Autowired
	private Properties properties;

	private String deezerAppId;
	private String deezerAppSecretKey;

	public DeezerApiAdapterImpl() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);
		this.httpClient = HttpClients.custom()
				.setConnectionManager(cm)
				.build();
	}

	@PostConstruct
	public void init() {
		deezerAppId = properties.getProperty("deezer.application.api.id");
		deezerAppSecretKey = properties.getProperty("deezer.application.api.secret.key");
	}

	@Override
	public String getUserAuthorizationPageUrl() {
		return String.format(DeezerApiConst.DEEZER_API_AUTH_BASE_URL, deezerAppId, DeezerApiConst.DEEZER_API_DEFAULT_REDIRECT_URI,
				DeezerApiConst.DEEZER_API_DEFAULT_PERMISSIONS);
	}

	private String makeApiRequest(String requestUrl) throws DeezerApiErrorException {
		HttpGet request = new HttpGet(requestUrl);
		LOG.info("api request: {}", request);

		try {
			HttpResponse response = httpClient.execute(request);
			LOG.info("api response: {}", response);
			// process response
			int statusCode = response.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK == statusCode) {
				String responseContent = StreamUtils.readStreamAsOneString(response.getEntity().getContent());
				LOG.debug("response content: {}", responseContent);
				return responseContent;
			}
		} catch (IOException e) {
			LOG.error("http request error: request = {}, exception = {}", request, e);
		} finally {
			request.releaseConnection();
		}

		throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + requestUrl);
	}

	private String makeApiRequest(String methodPath, Map<DeezerApiParam, String> params) throws DeezerApiErrorException {
		URIBuilder apiUriBuilder = new URIBuilder()
				.setScheme(DeezerApiConst.DEEZER_API_SCHEME)
				.setHost(DeezerApiConst.DEEZER_API_HOST);
		apiUriBuilder.clearParameters();
		params.forEach((key, value) -> apiUriBuilder.addParameter(key.getValue(), value));

		apiUriBuilder.setPath(methodPath);
		try {
			URI apiUri = apiUriBuilder.build();
			return makeApiRequest(apiUri.toString());
		} catch (URISyntaxException e) {
			LOG.error("construct uri error: ", e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
		}
	}

	private void putBaseRequestParameters(Map<DeezerApiParam, String> requestParameters,
																				String accessToken, Integer index, Integer limit) {
		if (accessToken != null) {
			requestParameters.put(DeezerApiParam.ACCESS_TOKEN, accessToken);
		}
		if (index != null) {
			requestParameters.put(DeezerApiParam.INDEX, String.valueOf(index));
		}
		if (limit != null) {
			requestParameters.put(DeezerApiParam.LIMIT, String.valueOf(limit));
		}
	}

	private <T> T convertJson(final String content, Class<T> targetClass) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(content, targetClass);
	}


	@Override
	public String getAccessToken(String code) {

		String accessTokenRequestUrl = String.format(DeezerApiConst.DEEZER_API_ACCESS_TOKEN_BASE_URL, deezerAppId, deezerAppSecretKey, code);

		try {
			String responseContent = makeApiRequest(accessTokenRequestUrl);
			if (responseContent != null) {
				Document responseDocument = XmlUtils.convertStringToXmlDocument(responseContent);
				String expression = ".//*[local-name() = 'access_token']";
				String accessToken = (String) XPathFactory.newInstance().newXPath()
						.evaluate(expression, responseDocument, XPathConstants.STRING);
				return accessToken;
			}
		} catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
			LOG.error("parsing xml from response error: ", e);
		} catch (DeezerApiErrorException e) {
			LOG.warn("getAccessToken error:", e);
		}
		return null;
	}

	@Override
	public Track getTrack(long trackId, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		if (accessToken != null) {
			requestParameters.put(DeezerApiParam.ACCESS_TOKEN, accessToken);
		}
		String methodPath = String.format(DeezerApiMethod.GET_TRACK.getValue(), trackId);
		String responseContent = makeApiRequest(methodPath, requestParameters);

		try {
			return convertJson(responseContent, Track.class);
		} catch (IOException e) {
			LOG.error("convert json response error: responseContent = {}, exception = {}", responseContent, e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
		}
	}

	@Override
	public Playlists getPlaylists(String accessToken, Integer index, Integer limit) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, index, limit);

		String methodPath = DeezerApiMethod.USER_ME_PLAYLISTS.getValue();
		String responseContent = makeApiRequest(methodPath, requestParameters);

		try {
			return convertJson(responseContent, Playlists.class);
		} catch (IOException e) {
			LOG.error("convert json response error: responseContent = {}, exception = {}", responseContent, e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
		}
	}

	@Override
	public Tracks getPlaylistTracks(long playlistId, String accessToken, Integer index, Integer limit) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, index, limit);

		String methodPath = String.format(DeezerApiMethod.GET_PLAYLIST_TRACKS.getValue(), playlistId);
		String responseContent = makeApiRequest(methodPath, requestParameters);

		try {
			return convertJson(responseContent, Tracks.class);
		} catch (IOException e) {
			LOG.error("convert json response error: responseContent = {}, exception = {}", responseContent, e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
		}
	}


}
