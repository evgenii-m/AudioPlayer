package ru.push.caudioplayer.core.deezer.impl;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
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

@Component
public class DeezerApiAdapterImpl implements DeezerApiAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiAdapterImpl.class);

	private final XPathFactory xPathFactory;
	private final URIBuilder baseApiUriBuilder;		// todo: think about make immutable builder
	private final HttpClient httpClient;

	@Autowired
	private Properties properties;

	private String deezerAppId;
	private String deezerAppSecretKey;

	public DeezerApiAdapterImpl() {
		this.xPathFactory = XPathFactory.newInstance();
		this.baseApiUriBuilder = new URIBuilder()
				.setScheme(DeezerApiConst.DEEZER_API_SCHEME)
				.setHost(DeezerApiConst.DEEZER_API_HOST);
		this.httpClient = HttpClientBuilder.create().build();
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
				LOG.info("response content: {}", responseContent);
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
		baseApiUriBuilder.clearParameters();
		params.forEach((key, value) -> baseApiUriBuilder.addParameter(key.getValue(), value));

		baseApiUriBuilder.setPath(methodPath);
		try {
			URI apiUri = baseApiUriBuilder.build();
			return makeApiRequest(apiUri.toString());
		} catch (URISyntaxException e) {
			LOG.error("construct uri error: ", e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
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
				String accessToken = (String) xPathFactory.newXPath().evaluate(expression, responseDocument, XPathConstants.STRING);
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
		if (accessToken != null) {
			requestParameters.put(DeezerApiParam.ACCESS_TOKEN, accessToken);
		}
		if (index != null) {
			requestParameters.put(DeezerApiParam.INDEX, String.valueOf(index));
		}
		if (limit != null) {
			requestParameters.put(DeezerApiParam.LIMIT, String.valueOf(limit));
		}
		String methodPath = DeezerApiMethod.USER_ME_PLAYLISTS.getValue();
		String responseContent = makeApiRequest(methodPath, requestParameters);

		try {
			return convertJson(responseContent, Playlists.class);
		} catch (IOException e) {
			LOG.error("convert json response error: responseContent = {}, exception = {}", responseContent, e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
		}
	}


}
