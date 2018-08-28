package com.mycorp.apikey.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.booleanProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.textProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;

import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationError;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationTemplate;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.BooleanPropertyDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appiancorp.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appiancorp.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// Must provide an integration id. This value need only be unique for this connected system
@TemplateId(name="LocationSearchIntegrationTemplate")
// Set template type to READ since this integration does not have side effects
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class LocationSearchIntegrationTemplate implements IntegrationTemplate {

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor integrationSystemConfigurationDescriptor,
      ConfigurationDescriptor connectedSystemConfigurationDescriptor,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {

    if (integrationSystemConfigurationDescriptor != null) {
      //No dynamic behavior for this integration. The only modifications come from user input
      return integrationSystemConfigurationDescriptor;
    }

    //Create user input fields
    TextPropertyDescriptor searchField = textProperty()
        .key("searchField")
        .label("Search Field")
        .instructionText("Please enter a name, address or phone number")
        .description("This will find results near your IP address")
        .placeholder("Grocery")
        .isRequired(true)
        .isExpressionable(true)
        .build();
    BooleanPropertyDescriptor phoneToggle = booleanProperty()
        .key("phoneToggle")
        .label("Phone Number?")
        .instructionText("Is your query a phone number?")
        .build();

    //Create the structured type that holds all of the integration information
    LocalTypeDescriptor localTypeDescriptor = type()
        .name("topLevelType")
        .properties(searchField, phoneToggle)
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
  public IntegrationResponse execute(
      ConfigurationDescriptor integrationConfigurationDescriptor,
      ConfigurationDescriptor connectedSystemConfigurationDescriptor,
      ExecutionContext executionContext) {
    //State objects contain the user configured values in the connected system and integration
    PropertyState connectedSystemState = connectedSystemConfigurationDescriptor.getRootState();
    PropertyState integrationState = integrationConfigurationDescriptor.getRootState();

    //Retrieve apiKey from the connected system
    String apiKey = (String)connectedSystemState.getValue(new PropertyPath("apiKey"));

    //Retrieve user input information from the integration
    String searchTerm = (String)integrationState.getValue(new PropertyPath("searchField"));
    Boolean phoneToggle = (Boolean)integrationState.getValue(new PropertyPath("phoneToggle"));

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
      Map<String,String> responseDiagnostic = getResponseDiagnostic(jsonResponse);
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

  private Map<String, String> getResponseDiagnostic(String jsonString) {
    Map<String, String> diagnostic = new HashMap<>();
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
