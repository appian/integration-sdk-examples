package com.mycorp.apikey.templates;

import static org.apache.http.impl.client.HttpClients.createDefault;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

public class PlacesClient implements AutoCloseable {

  private final CloseableHttpClient client;
  public static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";

  PlacesClient() {
    client = createDefault();
  }

  public CloseableHttpResponse execute(String apiKey, String searchTerm, Boolean phoneToggle)
      throws IOException, URISyntaxException {
    HttpGet getRequest = new HttpGet();
    try {
      String inputType = getInputType(phoneToggle);
      URI uri = constructRequest(apiKey, searchTerm, inputType);
      getRequest.setURI(uri);
      return client.execute(getRequest);
    } finally {
      getRequest.releaseConnection();
    }
  }

  private String getInputType(Boolean toggle) {
    if (toggle != null && toggle) {
      return "phonenumber";
    } else {
      return "textquery";
    }
  }

  private URI constructRequest(String apiKey, String searchTerm, String inputType)
      throws URISyntaxException {
    return new URIBuilder(BASE_URL)
        .addParameter("input", searchTerm)
        .addParameter("inputtype", inputType)
        .addParameter("fields", "formatted_address,name,rating,opening_hours")
        .addParameter("key", apiKey)
        .addParameter("locationbias", "ipbias")
        .build();
  }

  @Override
  public void close() {
    HttpClientUtils.closeQuietly(client);
  }
}
