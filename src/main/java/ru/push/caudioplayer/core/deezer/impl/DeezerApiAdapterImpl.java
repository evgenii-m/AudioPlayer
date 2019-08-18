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
import ru.push.caudioplayer.core.deezer.DeezerApiAdapter;
import ru.push.caudioplayer.core.deezer.DeezerApiConst;
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
public class DeezerApiAdapterImpl implements DeezerApiAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerApiAdapterImpl.class);


	private final XPathFactory xPathFactory;
	private final HttpClient httpClient;

	@Autowired
	private Properties properties;

	private String deezerAppId;
	private String deezerAppSecretKey;

	public DeezerApiAdapterImpl() {
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
		return String.format(DeezerApiConst.DEEZER_API_AUTH_BASE_URL, deezerAppId, DeezerApiConst.DEEZER_API_DEFAULT_REDIRECT_URI,
				DeezerApiConst.DEEZER_API_DEFAULT_PERMISSIONS);
	}

	@Override
	public String getAccessToken(String code) {

		String accessTokenRequestUrl = String.format(DeezerApiConst.DEEZER_API_ACCESS_TOKEN_BASE_URL, deezerAppId, deezerAppSecretKey, code);

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
