package com.mycorp.apikey.templates;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@TemplateId(name="APIKeyConnectedSystemTemplate")
public class APIKeyConnectedSystemTemplate extends SimpleTestableConnectedSystemTemplate {

  static String API_KEY = "apiKey";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    return simpleConfiguration.setProperties(
        // Sensitive values should use an encryptedTextProperty
        encryptedTextProperty(API_KEY)
        .label("API Key")
        .instructionText("See https://developers.google.com/places/web-service/get-api-key for instructions to generate a Google API Key")
        .build()
    );
  }

  @Override
  protected TestConnectionResult testConnection(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    String apiKey = simpleConfiguration.getValue(API_KEY);
    try (PlacesClient client = new PlacesClient()) {
      //Execute simple call to Google Places API
      CloseableHttpResponse response = client.execute(apiKey, "", false);

      //Read entity directly to json string
      HttpEntity entity = response.getEntity();
      String jsonResponse = EntityUtils.toString(entity);
      EntityUtils.consume(entity);

      Map<String, Object> responseMap = getResponseMap(jsonResponse);
      //Determine if Google returned an error
      if (response.getStatusLine().getStatusCode() != 200 || responseMap.containsKey(("error_message"))) {
        String errorMessage = (String)responseMap.get("error_message");
        return TestConnectionResult.error(errorMessage);
      }
      return TestConnectionResult.success();
    } catch (IOException | URISyntaxException e) {
      return TestConnectionResult.error("Something went wrong: " + e.getMessage());
    }
  }

  //Deserialize JSON to Java Map
  private Map<String,Object> getResponseMap(String jsonResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String,Object> responseMap = objectMapper.readValue(jsonResponse, new TypeReference<HashMap<String,Object>>() {});
    return responseMap;
  }
}
