package com.mycorp.googledrive.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.oauth.ExpiredTokenException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.model.File;
import com.google.common.base.Stopwatch;

public class IntegrationExecutionUtils {
  private IntegrationExecutionUtils() {
  }

  /**
   * Creates common fields of diagnostics objects for both Send File and Create Folder templates.
   * Diagnostic is information that will be displayed in the Request and Response tabs. You can
   * include information that is helpful for the developer to debug, such as HTTP parameters.
   */
  public static Map<String,Object> getRequestDiagnostics(SimpleConfiguration connectedSystemConfiguration) {
    Map<String,Object> requestDiagnostic = new HashMap<>();
    //Request Diagnostic values will be shown on the Request tab on Appian Integration Designer Interface,
    //which will be visible to designers. Only add to diagnostics values that you wish the designer to see.
    String clientId = connectedSystemConfiguration.getValue(
        GoogleDriveSampleConnectedSystemTemplate.CLIENT_ID_KEY);
    requestDiagnostic.put(GoogleDriveSampleConnectedSystemTemplate.CLIENT_ID_KEY, clientId);
    //For sensitive values, mask it so that it won't be visible to designers
    requestDiagnostic.put(GoogleDriveSampleConnectedSystemTemplate.CLIENT_SECRET_KEY, "******************");
    return requestDiagnostic;
  }

  /**
   * Creates response diagnostics for both {@link GoogleDriveSendFileIntegrationTemplate} and
   * {@link GoogleDriveCreateFolderIntegrationTemplate}
   */
  public static Map<String,Object> getResponseDiagnostics(File responseFile) {
    Map<String,Object> responseDiagnostic = new HashMap<>();
    responseDiagnostic.put("Name", responseFile.getName());
    responseDiagnostic.put("ID", responseFile.getId());
    responseDiagnostic.put("MIME Type", responseFile.getMimeType());
    return responseDiagnostic;
  }

  /**
   * This information will be shown in the Result Tab. These keys can also be keyed off in the Process Modeler
   * when executing the integration.
   */
  public static Map<String,Object> getResult(File responseFile) {
    Map<String,Object> result = new HashMap<>();
    result.put("fileName", responseFile.getName());
    result.put("fileID", responseFile.getId());
    result.put("fileMimeType", responseFile.getMimeType());
    return result;
  }

  /**
   * Handles Google's {@link GoogleJsonResponseException}. You can add custom logic for handling
   * specific errors, such as building a IntegrationResponse with different error messages for a
   * particular error code.
   */
  public static IntegrationResponse handleException(
      GoogleJsonResponseException e,
      IntegrationDesignerDiagnostic.IntegrationDesignerDiagnosticBuilder diagnosticBuilder,
      Map<String,Object> requestDiagnostics,
      Stopwatch stopwatch) {
    if (e.getStatusCode() == 401) {
      //Google returns a 401 exception if your credential is not authorized or expired. Throw an
      //ExpiredTokenException when this happens, Appian will try to refresh the token.
      throw new ExpiredTokenException();
    }
    long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
    IntegrationDesignerDiagnostic diagnostic = diagnosticBuilder.addExecutionTimeDiagnostic(elapsed)
        .addRequestDiagnostic(requestDiagnostics)
        .build();

    IntegrationError error = IntegrationError.builder()
        .title("Google Exception Status: " + e.getStatusMessage())
        .message(e.getMessage())
        .build();

    return IntegrationResponse.forError(error).withDiagnostic(diagnostic).build();
  }
}
