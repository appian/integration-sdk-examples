package com.mycorp.apikey.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.encryptedTextProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;
import com.appiancorp.connectedsystems.templateframework.sdk.diagnostic.TestConnectionResult;
import com.appiancorp.connectedsystems.templateframework.sdk.service.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.service.annotation.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.service.v2.TestableConnectedSystemTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@TemplateId("APIKeyConnectedSystemTemplate")
public class APIKeyConnectedSystemTemplate implements TestableConnectedSystemTemplate {

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor configurationDescriptor,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {

    if(configurationDescriptor != null) {
      //No dynamic behavior for this connected system. The only modifications come from user input
      return configurationDescriptor;
    }

    //Create the structured type that holds all of the connected system information
    LocalTypeDescriptor localTypeDescriptor = type()
        .name("topLevelType")
        .properties(
            //Sensitive values should use an encryptedTextProperty()
            encryptedTextProperty()
                .key("apiKey")
                .label("API Key")
                .instructionText("See https://developers.google.com/places/web-service/get-api-key for instructions to generate a Google API Key")
                .build()
        )
        .build();

    //Generates the initial PropertyState object for the given local type
    StateGenerator stateGenerator = new StateGenerator(localTypeDescriptor);
    PropertyState initialState = stateGenerator.generateDefaultState(localTypeDescriptor);

    return ConfigurationDescriptor.builder()
        .withState(initialState)
        .withTypes(localTypeDescriptor)
        .version(1)
        .build();
  }

  @Override
  public TestConnectionResult testConnection(
      ConfigurationDescriptor configurationDescriptor, ExecutionContext executionContext) {
    PropertyState connectedSystemState = configurationDescriptor.getRootState();
    PropertyPath pathToAPIKey = new PropertyPath("apiKey");

    //Retrieve user entered api key from PropertyState
    String apiKey = (String)connectedSystemState.getValue(pathToAPIKey);

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
