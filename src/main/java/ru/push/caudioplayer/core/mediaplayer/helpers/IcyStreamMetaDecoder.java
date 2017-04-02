package ru.push.caudioplayer.core.mediaplayer.helpers;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author push <mez.e.s@yandex.ru>
 *   based on IcyStreamMeta class from https://github.com/dazza5000/IcyStreamMetaDataExample
 * @date 3/16/17
 */
class IcyStreamMetaDecoder {

  private URL streamUrl;
  private Map<String, String> requestProperties;
  private Set<String> supportedContentTypes;
  private Map<String, String> metadata;
  private boolean isError;
  private Map<String, String> data;

  IcyStreamMetaDecoder(String resourcePath, Map<String, String> requestProperties, Set<String> supportedContentTypes)
      throws MalformedURLException {
    isError = false;
    streamUrl = new URL(resourcePath);
    this.requestProperties = requestProperties;
    this.supportedContentTypes = supportedContentTypes;
  }

  IcyStreamMetaDecoder(String resourcePath) throws MalformedURLException {
    isError = false;
    streamUrl = new URL(resourcePath);

    requestProperties = new HashMap<>();
    requestProperties.put("Icy-MetaData", "1");
    requestProperties.put("Connection", "close");
    requestProperties.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

    supportedContentTypes = new HashSet<>();
    supportedContentTypes.add("audio/mpeg");
  }


  public boolean isError() {
    return isError;
  }

  public URL getStreamUrl() {
    return streamUrl;
  }

  public void setStreamUrl(URL streamUrl) {
    this.streamUrl = streamUrl;
    metadata = null;
    isError = false;
  }

  public void setRequestProperties(Map<String, String> requestProperties) {
    this.requestProperties = requestProperties;
    metadata = null;
    isError = false;
  }

  public void setSupportedContentTypes(Set<String> supportedContentTypes) {
    this.supportedContentTypes = supportedContentTypes;
    metadata = null;
    isError = false;
  }

  public String getStreamTitle() throws IOException {
    data = getMetadata();
    return data.getOrDefault("StreamTitle", "");
  }

  public String getStationName() throws IOException {
    data = getMetadata();
    return data.getOrDefault("icy-name", "");
  }


  public Map<String, String> getMetadata() throws IOException {
    if (metadata == null) {
      refreshMeta();
    }

    return metadata;
  }

  public void refreshMeta() throws IOException {
    retrieveMetadata();
  }

  private HttpURLConnection makeHttpUrlConnection(URL url) throws IOException {
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    requestProperties.entrySet().forEach(entry ->
        con.setRequestProperty(entry.getKey(), entry.getValue())
    );
    con.connect();
    return con;
  }

  private void retrieveMetadata() throws IOException {
    HttpURLConnection con = makeHttpUrlConnection(streamUrl);

    // different servers may transfer the same header fields in different string cases,
    // so convert them to lower case to avoid problems in further processing of fields
    Map<String, List<String>> headers = new HashMap<>();
    con.getHeaderFields().entrySet().forEach(entry -> {
      String keyLowerCase = StringUtils.lowerCase(entry.getKey());
      headers.put(keyLowerCase, entry.getValue());
    });

    if (!headers.containsKey("content-type") || supportedContentTypes.retainAll(headers.get("content-type"))) {
      throw new IOException("Unsupported content type [contentType = " + headers.get("content-type"));
    }

    InputStream stream = con.getInputStream();
    int metaDataOffset = 0;
    if (headers.containsKey("icy-metaint")) {
      // Headers are sent via HTTP
      metaDataOffset = Integer.parseInt(headers.get("icy-metaint").get(0));
    } else {
      // Headers are sent within a stream
      StringBuilder strHeaders = new StringBuilder();
      char c;
      // TODO: fix potential OutOfMemory exception (can happen when invalid header in input stream)
      while ((c = (char) stream.read()) != -1) {
        strHeaders.append(c);
        if (strHeaders.length() > 5
            && (strHeaders.substring((strHeaders.length() - 4), strHeaders.length()).equals("\r\n\r\n"))) {
          // end of headers
          break;
        }
      }

      // Match headers to get metadata offset within a stream
      Pattern p = Pattern.compile("\\r\\n(icy-metaint):\\s*(.*)\\r\\n");
      Matcher m = p.matcher(strHeaders.toString());
      if (m.find()) {
        metaDataOffset = Integer.parseInt(m.group(2));
      }
    }

    // In case no data was sent
    if (metaDataOffset == 0) {
      isError = true;
      return;
    }

    // Read metadata
    int b;
    int count = 0;
    int metaDataLength = 4080; // 4080 is the max length
    boolean inData = false;
    StringBuilder metaData = new StringBuilder();
    // Stream position should be either at the beginning or right after headers
    while ((b = stream.read()) != -1) {
      count++;

      // Length of the metadata
      if (count == metaDataOffset + 1) {
        metaDataLength = b * 16;
      }

      if (count > metaDataOffset + 1 && count < (metaDataOffset + metaDataLength)) {
        inData = true;
      } else {
        inData = false;
      }
      if (inData) {
        if (b != 0) {
          metaData.append((char) b);
        }
      }
      if (count > (metaDataOffset + metaDataLength)) {
        break;
      }
    }

    // Set the data
    metadata = IcyStreamMetaDecoder.parseMetadata(metaData.toString());

    // Close
    stream.close();

  }

  public static Map<String, String> parseMetadata(String metaString) {
    Map<String, String> metadata = new HashMap();
    String[] metaParts = metaString.split(";");
    Pattern p = Pattern.compile("^([a-zA-Z]+)=\\'([^\\']*)\\'$");
    Matcher m;
    for (int i = 0; i < metaParts.length; i++) {
      m = p.matcher(metaParts[i]);
      if (m.find()) {
        metadata.put((String) m.group(1), (String) m.group(2));
      }
    }

    return metadata;
  }
}
