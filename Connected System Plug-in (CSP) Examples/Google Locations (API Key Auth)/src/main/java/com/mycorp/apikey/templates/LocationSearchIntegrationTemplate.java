package com.mycorp.apikey.templates;

import static com.mycorp.apikey.templates.APIKeyConnectedSystemTemplate.API_KEY;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// Must provide an integration id. This value need only be unique for this connected system
@TemplateId(name="LocationSearchIntegrationTemplate")
// Set template type to READ since this integration does not have side effects
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class LocationSearchIntegrationTemplate extends SimpleIntegrationTemplate {

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    return integrationConfiguration.setProperties(
        // Create user input fields
        textProperty("searchField")
          .label("Search Field")
          .instructionText("Please enter a name, address or phone number")
          .description("This will find results near your IP address")
          .placeholder("Grocery")
          .isRequired(true)
          .isExpressionable(true)
          .build(),
        booleanProperty("phoneToggle")
          .label("Phone Number?")
          .instructionText("Is your query a phone number?")
          .build()
    );
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    // The apiKey is stored in the Connected System and all integrations for that Connected Systems will share credentials
    String apiKey = connectedSystemConfiguration.getValue(API_KEY);

    // The search term and phonetoggle are both specific to the integration
    String searchTerm = integrationConfiguration.getValue("searchField");
    Boolean phoneToggle = integrationConfiguration.getValue("phoneToggle");

    IntegrationResponse.Builder integrationResponseBuilder;
    CloseableHttpResponse httpResponse = null;
    try (PlacesClient client = new PlacesClient()){
      //The amount of time it takes to interact with the external
      // system will be displayed to the end user
      long startTime = System.currentTimeMillis();
      //Execute call to Google Places API
      httpResponse = client.execute(apiKey, searchTerm, phoneToggle);
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;

      String jsonResponse = EntityUtils.toString(httpResponse.getEntity());

      //Determine if call was successful
      if (httpResponse.getStatusLine().getStatusCode() == 200) {
        Map<String,Object> responseMap = getResponseMap(jsonResponse);
        //Google will send a 200 even if execution resulted in what is considered an error by this template
        if (responseMap.containsKey("error_message")) {
          IntegrationError error = googleReturnedError(responseMap);
          //Builds response for error case. Status code may not always correspond to success or failure
          integrationResponseBuilder = IntegrationResponse.forError(error);
        } else {
          //Builds response for success case
          integrationResponseBuilder = IntegrationResponse.forSuccess(responseMap);
        }
      } else {
        //Builds response for error case
        IntegrationError error = httpResponseError(httpResponse);
        integrationResponseBuilder = IntegrationResponse.forError(error);
      }

      //Gets request and response information to display to help user diagnose problems with the integration
      Map<String,Object> requestDiagnostic = getRequestDiagnostic(apiKey, searchTerm, phoneToggle);
      Map<String,Object> responseDiagnostic = getResponseDiagnostic(jsonResponse);
      IntegrationDesignerDiagnostic integrationDesignerDiagnostic = IntegrationDesignerDiagnostic.builder()
          .addRequestDiagnostic(requestDiagnostic)
          .addResponseDiagnostic(responseDiagnostic)
          .addExecutionTimeDiagnostic(executionTime)
          .build();
      return integrationResponseBuilder.withDiagnostic(integrationDesignerDiagnostic).build();
    } catch (URISyntaxException | IOException e) {
      //Builds default response for unknown error case
      IntegrationError error = templateError();
      return IntegrationResponse.forError(error).build();
    } finally {
      //Closes Http Response
      HttpClientUtils.closeQuietly(httpResponse);
    }
  }

  private Map<String, Object> getRequestDiagnostic(String apiKey, String searchField, Boolean phoneToggle) {
    Map<String, Object> diagnostic = new HashMap<>();
    diagnostic.put("Url", PlacesClient.BASE_URL);
    diagnostic.put("Key", "***********");
    diagnostic.put("Search Term", searchField);
    diagnostic.put("Is Phone Number", phoneToggle);
    return diagnostic;
  }

  private Map<String, Object> getResponseDiagnostic(String jsonString) {
    Map<String, Object> diagnostic = new HashMap<>();
    diagnostic.put("Raw Response", jsonString);
    return diagnostic;
  }

  private IntegrationError googleReturnedError(Map<String,Object> responseMap) {
    return IntegrationError.builder()
        .title("Error with search")
        .message((String)responseMap.get("error_message"))
        .build();
  }

  private IntegrationError httpResponseError(CloseableHttpResponse httpResponse) {
    return IntegrationError.builder()
        .title("Received an error Response")
        .message("Status Code: " + httpResponse.getStatusLine().getStatusCode())
        .build();
  }

  private IntegrationError templateError() {
    return IntegrationError.builder()
        .title("Something went wrong")
        .message("An error occurred in the IntegrationTemplate")
        .build();
  }

  private Map<String,Object> getResponseMap(String jsonResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String,Object> responseMap = objectMapper.readValue(jsonResponse,
        new TypeReference<HashMap<String,Object>>() {
        });
    return responseMap;
  }
}
