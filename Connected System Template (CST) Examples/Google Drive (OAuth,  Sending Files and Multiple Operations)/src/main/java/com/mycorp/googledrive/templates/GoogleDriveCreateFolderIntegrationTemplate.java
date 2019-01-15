package com.mycorp.googledrive.templates;

import static com.mycorp.googledrive.templates.GoogleDriveSendFileIntegrationTemplate.NAME_KEY;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.base.Stopwatch;

@TemplateId(name="GoogleDriveCreateFolderIntegrationTemplate")
public class GoogleDriveCreateFolderIntegrationTemplate extends SimpleIntegrationTemplate {

  private static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    return integrationConfiguration.setProperties(
        //The same key (NAME_KEY) is used in the other template to create a textProperty for document name,
        // therefore when you toggle between operations, the value you enter in this field will persist.
        textProperty(NAME_KEY)
            .label("Folder Name")
            .isRequired(true)
            .build()
    );
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    String folderName = integrationConfiguration.getValue(NAME_KEY);

    IntegrationDesignerDiagnostic.IntegrationDesignerDiagnosticBuilder diagnosticBuilder =  IntegrationDesignerDiagnostic.builder();
    Stopwatch stopwatch = Stopwatch.createStarted();

    //GoogleCredential is used to create a Google client with Drive. In Google Drive API, folders and files
    //are both a File object. To create a folder, you need to supply a File object with a folder name and folder
    //MIME type
    GoogleCredential credential = new GoogleCredential().setAccessToken(executionContext.getAccessToken().get());
    Drive drive = new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).build();
    File file = new File().setName(folderName).setMimeType(FOLDER_MIME_TYPE);
    File responseFile;
    try {
      responseFile = drive
          .files()
          .create(file)
          .setFields("name, id, mimeType")
          .execute();
    } catch (GoogleJsonResponseException e) {
      Map<String,Object> requestDiagnostics = getRequestDiagnostics(
          connectedSystemConfiguration, integrationConfiguration);
      return IntegrationExecutionUtils.handleException(e, diagnosticBuilder,
          requestDiagnostics, stopwatch);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Map<String,Object> requestDiagnostics = getRequestDiagnostics(
        connectedSystemConfiguration, integrationConfiguration);

    Map<String,Object> diagnosticResponse = IntegrationExecutionUtils.getResponseDiagnostics(responseFile);
    IntegrationDesignerDiagnostic diagnostic = diagnosticBuilder
        .addExecutionTimeDiagnostic(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS))
        .addRequestDiagnostic(requestDiagnostics)
        .addResponseDiagnostic(diagnosticResponse)
        .build();

    Map<String, Object> result = IntegrationExecutionUtils.getResult(responseFile);
    return IntegrationResponse
        .forSuccess(result)
        .withDiagnostic(diagnostic)
        .build();
  }

  private static Map<String,Object> getRequestDiagnostics(SimpleConfiguration connectedSystemConfiguration, SimpleConfiguration integrationConfiguration) {
    Map<String,Object> requestDiagnostics = IntegrationExecutionUtils.getRequestDiagnostics(connectedSystemConfiguration);
    String folderName = integrationConfiguration.getValue(NAME_KEY);
    requestDiagnostics.put("Folder Name", folderName);
    return requestDiagnostics;
  }
}

