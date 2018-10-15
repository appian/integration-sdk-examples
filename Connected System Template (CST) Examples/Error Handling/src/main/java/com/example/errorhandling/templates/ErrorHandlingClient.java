package com.example.errorhandling.templates;

import static org.apache.http.impl.client.HttpClients.createDefault;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

public class ErrorHandlingClient implements AutoCloseable {
  private static final String HTTPS_BASE_URL = "https://httpbin.org";
  private final CloseableHttpClient client;

  public ErrorHandlingClient(){
    client = createDefault();
  }

  @Override
  public void close() throws Exception {
    client.close();
  }

  public CloseableHttpResponse execute(String statusCode) throws IOException, URISyntaxException {
    HttpGet getRequest = new HttpGet();
    getRequest.setURI(createURI(statusCode));
    return client.execute(getRequest);
  }

  public static URI createURI(String statusCode) throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder(HTTPS_BASE_URL);
    String statusPath = String.format("/status/%s", statusCode);
    URI uri = uriBuilder.setPath(statusPath).build();
    return uri;
  }
}
