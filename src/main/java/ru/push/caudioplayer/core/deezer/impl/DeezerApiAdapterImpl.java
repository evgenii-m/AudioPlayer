package ru.push.caudioplayer.core.deezer.impl;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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
import ru.push.caudioplayer.core.deezer.DeezerApiRequestMethodType;
import ru.push.caudioplayer.core.deezer.model.Playlist;
import ru.push.caudioplayer.core.deezer.model.Playlists;
import ru.push.caudioplayer.core.deezer.model.Track;
import ru.push.caudioplayer.core.deezer.model.Tracks;
import ru.push.caudioplayer.core.deezer.model.internal.PlaylistId;
import ru.push.caudioplayer.utils.StreamUtils;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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

	private String makeApiRequest(HttpRequestBase request) throws DeezerApiErrorException {
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

		throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + request);
	}

	private String makeApiGetRequest(String requestUrl) throws DeezerApiErrorException {
		HttpGet request = new HttpGet(requestUrl);
		return makeApiRequest(request);
	}

	private String makeApiPostRequest(String requestUrl, String requestBody) throws DeezerApiErrorException {
		HttpPost request = new HttpPost(requestUrl);
		try {
			if ((requestBody != null) && !requestBody.isEmpty()) {
				request.setEntity(new ByteArrayEntity(requestBody.getBytes("UTF-8")));
			}
			return makeApiRequest(request);
		} catch (UnsupportedEncodingException e) {
			LOG.error("request error: ", e);
			return null;
		}
	}

	private String makeApiDeleteRequest(String requestUrl) throws DeezerApiErrorException {
		HttpDelete request = new HttpDelete(requestUrl);
		return makeApiRequest(request);
	}

	private String makeApiRequest(String methodPath, DeezerApiRequestMethodType methodType,
																		Map<DeezerApiParam, String> params) throws DeezerApiErrorException {

		URIBuilder apiUriBuilder = new URIBuilder()
				.setScheme(DeezerApiConst.DEEZER_API_SCHEME)
				.setHost(DeezerApiConst.DEEZER_API_HOST);
		apiUriBuilder.clearParameters();
		params.forEach((key, value) -> apiUriBuilder.addParameter(key.getValue(), value));

		apiUriBuilder.setPath(methodPath);
		try {
			URI apiUri = apiUriBuilder.build();

			switch (methodType) {
				case POST:
					return makeApiPostRequest(apiUri.toString(), null);
				case DELETE:
					return makeApiDeleteRequest(apiUri.toString());
				case GET:
				default:
					return makeApiGetRequest(apiUri.toString());
			}
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

	private <T> T convertJson(final String content, Class<T> targetClass, String methodPath) throws DeezerApiErrorException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			T result = mapper.readValue(content, targetClass);
			if (result == null) {
				LOG.warn("Json converting result is null: content = {}", content);
			}
			return result;
		} catch (IOException e) {
			LOG.error("convert json response error: responseContent = {}, exception = {}", content, e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
		}
	}

	private <T> String convertToJson(T object, String methodPath) throws DeezerApiErrorException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(object);
		} catch (IOException e) {
			LOG.error("convert json response error: object = {}, exception = {}", object, e);
			throw new DeezerApiErrorException("Deezer api - Not acceptable result for request: " + methodPath);
		}
	}


	@Override
	public String getAccessToken(String code) {

		String accessTokenRequestUrl = String.format(DeezerApiConst.DEEZER_API_ACCESS_TOKEN_BASE_URL, deezerAppId, deezerAppSecretKey, code);

		try {
			String responseContent = makeApiGetRequest(accessTokenRequestUrl);
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
		String methodPath = DeezerApiMethod.GET_TRACK.formate(trackId);
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.GET_TRACK.getMethodType(), requestParameters);

		return convertJson(responseContent, Track.class, methodPath);
	}

	@Override
	public Playlists getPlaylists(String accessToken, Integer index, Integer limit) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, index, limit);

		String methodPath = DeezerApiMethod.USER_PLAYLISTS.getValue();
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.USER_PLAYLISTS.getMethodType(), requestParameters);

		return convertJson(responseContent, Playlists.class, methodPath);
	}

	@Override
	public Playlist getPlaylist(long playlistId, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);

		String methodPath = DeezerApiMethod.GET_PLAYLIST.formate(playlistId);
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.GET_PLAYLIST.getMethodType(), requestParameters);

		return convertJson(responseContent, Playlist.class, methodPath);
	}

	@Override
	public Tracks getPlaylistTracks(long playlistId, String accessToken, Integer index, Integer limit) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, index, limit);

		String methodPath = DeezerApiMethod.GET_PLAYLIST_TRACKS.formate(playlistId);
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.GET_PLAYLIST_TRACKS.getMethodType(), requestParameters);

		return convertJson(responseContent, Tracks.class, methodPath);
	}

	@Override
	public PlaylistId createPlaylist(String title, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);
		requestParameters.put(DeezerApiParam.TITLE, title);

		String methodPath = DeezerApiMethod.USER_PLAYLIST_CREATE.getValue();
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.USER_PLAYLIST_CREATE.getMethodType(), requestParameters);

		return convertJson(responseContent, PlaylistId.class, methodPath);
	}

	@Override
	public boolean deletePlaylist(long playlistId, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);

		String methodPath = DeezerApiMethod.DELETE_USER_PLAYLIST.formate(playlistId);
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.DELETE_USER_PLAYLIST.getMethodType(), requestParameters);

		return Boolean.valueOf(responseContent);
	}

	@Override
	public boolean renamePlaylist(long playlistId, String newTitle, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);
		requestParameters.put(DeezerApiParam.TITLE, newTitle);

		String methodPath = DeezerApiMethod.UPDATE_USER_PLAYLIST.formate(playlistId);
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.UPDATE_USER_PLAYLIST.getMethodType(), requestParameters);

		return Boolean.valueOf(responseContent);
	}

	@Override
	public boolean addTracksToPlaylist(long playlistId, List<Long> trackIds, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);
		String trackIdsString = trackIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		requestParameters.put(DeezerApiParam.SONGS, trackIdsString);

		String methodPath = DeezerApiMethod.PLAYLIST_ADD_TRACK.formate(playlistId);
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.PLAYLIST_ADD_TRACK.getMethodType(), requestParameters);

		return Boolean.valueOf(responseContent);
	}

	@Override
	public boolean removeTracksFromPlaylist(long playlistId, List<Long> trackIds, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);
		String trackIdsString = trackIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		requestParameters.put(DeezerApiParam.SONGS, trackIdsString);

		String methodPath = DeezerApiMethod.PLAYLIST_REMOVE_TRACK.formate(playlistId);
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.PLAYLIST_REMOVE_TRACK.getMethodType(), requestParameters);

		return Boolean.valueOf(responseContent);
	}

	@Override
	public Tracks searchTracksQuery(String query, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);
		requestParameters.put(DeezerApiParam.QUERY, query);

		String methodPath = DeezerApiMethod.SEARCH_TRACK_QUERY.getValue();
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.SEARCH_TRACK_QUERY.getMethodType(), requestParameters);

		return convertJson(responseContent, Tracks.class, methodPath);
	}

	@Override
	public boolean addTrackToFavorites(long trackId, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);
		requestParameters.put(DeezerApiParam.TRACK_ID, String.valueOf(trackId));

		String methodPath = DeezerApiMethod.USER_FAVORITES_ADD_TRACK.getValue();
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.USER_FAVORITES_ADD_TRACK.getMethodType(), requestParameters);

		return Boolean.valueOf(responseContent);
	}

	@Override
	public boolean removeTrackFromFavorites(long trackId, String accessToken) throws DeezerApiErrorException {
		Map<DeezerApiParam, String> requestParameters = new HashMap<>();
		putBaseRequestParameters(requestParameters, accessToken, null, null);
		requestParameters.put(DeezerApiParam.TRACK_ID, String.valueOf(trackId));

		String methodPath = DeezerApiMethod.USER_FAVORITES_REMOVE_TRACK.getValue();
		String responseContent = makeApiRequest(methodPath, DeezerApiMethod.USER_FAVORITES_REMOVE_TRACK.getMethodType(), requestParameters);

		return Boolean.valueOf(responseContent);
	}

}
