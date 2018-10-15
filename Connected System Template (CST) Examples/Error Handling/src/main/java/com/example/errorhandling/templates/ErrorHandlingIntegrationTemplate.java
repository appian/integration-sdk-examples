package com.example.errorhandling.templates;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;

import com.appiancorp.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appiancorp.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationError;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.RefreshPolicy;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appiancorp.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appiancorp.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;

// Must provide an integration id. This value need only be unique for this connected system
@TemplateId(name = "ErrorHandlingIntegrationTemplate")
// Set template type to READ since this integration does not have side effects
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class ErrorHandlingIntegrationTemplate extends SimpleIntegrationTemplate {

  /**
   * This is an example of an Integration Template that handles Configuration Errors and Execution Errors.
   *
   * Configuration Errors refer to validation errors that happen on the Property Descriptors, such as
   * phone number, email, and zip code validations. These error messages will be shown in red under the specified
   * field after the UI refreshes.
   *
   * Execution Errors refer to errors that happen when making HTTP external system calls, such as IOException and
   * HTTP response errors. These errors will be shown in a red error box in the result tab after execution runs.
   *
   * In this example, we will show you how to handle Configuration Errors by validating a phone number text field.
   * To qualify, the phone number's length must be 10 and it must be all numeric numbers. We will also show you
   * how to handle Execution Errors by handling responses from https://www.httpbin.org (an http endpoint that
   * lets you simulates http responses).
   */

  public static final String PHONE_NUMBER_KEY = "phoneNumber";
  public static final String HTTP_STATUS_CODE_KEY = "httpStatusCode";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    TextPropertyDescriptor phoneNumberDescriptor = textProperty(PHONE_NUMBER_KEY).label("Phone Number")
        .refresh(RefreshPolicy.ALWAYS)
        .instructionText("Please enter a 10 digit numbers in this format")
        .build();

    TextPropertyDescriptor httpStatusCodeDescriptor = textProperty(HTTP_STATUS_CODE_KEY).label("HTTP Status Code")
        .choices(createStatusCodeChoices())
        .isRequired(true)
        .build();

    integrationConfiguration.setProperties(phoneNumberDescriptor, httpStatusCodeDescriptor);

    //Phone number validation
    List<String> errorMessages = getConfigurationValidationErrorsForPhoneNumber(integrationConfiguration);
    integrationConfiguration.setErrors(PHONE_NUMBER_KEY, errorMessages);

    return integrationConfiguration;
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    String statusCode = integrationConfiguration.getValue(HTTP_STATUS_CODE_KEY);

    Map<String,Object> requestDiagnosticsMap = new HashMap<>(); //diagnostic data to show under request tab
    Map<String, String> responseDiagnosticsMap = new HashMap<>(); //diagnostic data to show under response tab
    IntegrationError.IntegrationErrorBuilder integrationErrorBuilder = new IntegrationError.IntegrationErrorBuilder();

    IntegrationDesignerDiagnostic.IntegrationDesignerDiagnosticBuilder diagnosticBuilder = IntegrationDesignerDiagnostic
        .builder();

    try {
      requestDiagnosticsMap.put("URL", ErrorHandlingClient.getURI(statusCode));
      diagnosticBuilder.addRequestDiagnostic(requestDiagnosticsMap);

      ErrorHandlingClient errorClient = new ErrorHandlingClient();
      CloseableHttpResponse response = errorClient.execute(statusCode);
      responseDiagnosticsMap.put("Response", response.toString());
      diagnosticBuilder.addResponseDiagnostic(responseDiagnosticsMap);

      //Handle Http Response Errors
      if (response.getStatusLine().getStatusCode() >= 400) {
        integrationErrorBuilder.title(Integer.toString(response.getStatusLine().getStatusCode()))
            .message(response.getStatusLine().getReasonPhrase());
        return IntegrationResponse.forError(integrationErrorBuilder.build()).withDiagnostic(diagnosticBuilder.build()).build();
      }

      Map<String,Object> responseMap = new HashMap<>();
      responseMap.put("Status Code: ", response.getStatusLine().getStatusCode());
      responseMap.put("Reason Phrase: ", response.getStatusLine().getReasonPhrase());
      return IntegrationResponse.forSuccess(responseMap)
          .withDiagnostic(diagnosticBuilder.build())
          .build();
    } catch (URISyntaxException | IOException e) {
      integrationErrorBuilder.title("https://www.httpbin.org returns the following exception:");
      integrationErrorBuilder.message(e.getMessage());
      responseDiagnosticsMap.put("Error Response", e.toString());
      diagnosticBuilder.addResponseDiagnostic(responseDiagnosticsMap);
      return IntegrationResponse.forError(integrationErrorBuilder.build())
          .withDiagnostic(diagnosticBuilder.build())
          .build();
    }
  }

  /**
   * Generate a list of Choice that represent http status codes
   */
  private Choice[] createStatusCodeChoices() {
    //Use Java reflection library to get all http status codes defined in HttpStatus class
    Field[] fields = HttpStatus.class.getDeclaredFields();
    return Arrays.stream(fields).map(field -> {
      try {
        int statusCode = field.getInt(null);
        String choiceName = field.getName() + ": " + statusCode;
        return DomainSpecificLanguage.choice().name(choiceName).value(Integer.toString(statusCode)).build();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }).toArray(Choice[]::new);
  }

  /**
   * Generate validation error messages for phone number
   */
  private List<String> getConfigurationValidationErrorsForPhoneNumber(SimpleConfiguration integrationConfiguration) {
    String phoneNumber = integrationConfiguration.getValue(PHONE_NUMBER_KEY);
    List<String> errorMessages = new ArrayList<>();
    if (phoneNumber != null) {
      if (phoneNumber.length() != 10) {
        errorMessages.add("Please make sure your phone number is 10 digits long");
      }
      if (!phoneNumber.matches("[0-9]+")) {
        //Regex pattern [0-9]+ matches any numeric numbers
        errorMessages.add("Please make sure your phone number is only numeric characters");
      }
    }
    return errorMessages;
  }
}
