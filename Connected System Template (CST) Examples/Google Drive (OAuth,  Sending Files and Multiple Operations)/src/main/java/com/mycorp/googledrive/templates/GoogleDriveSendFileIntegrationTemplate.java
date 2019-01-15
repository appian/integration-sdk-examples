package com.mycorp.googledrive.templates;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.common.base.Stopwatch;

@TemplateId(name = "GoogleDriveSendFileIntegrationTemplate")
public class GoogleDriveSendFileIntegrationTemplate extends SimpleIntegrationTemplate {

  public static final String FILE_KEY = "fileId";
  public static final String NAME_KEY = "fileName";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    return integrationConfiguration.setProperties(
        //Document Property allows the user to select a document in Appian to send to Google Drive
        documentProperty(FILE_KEY).label("File").isRequired(true).build(),
        //The same key (NAME_KEY) is used in the other template to create a textProperty for folder name,
        //therefore when you toggle between operations, the value you enter in this field will persist.
        textProperty(NAME_KEY).label("File Name")
            .instructionText("If left blank, the document's Appian name will be used instead")
            .build());
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    Document document = integrationConfiguration.getValue(FILE_KEY);

    IntegrationDesignerDiagnostic.IntegrationDesignerDiagnosticBuilder diagnosticBuilder = IntegrationDesignerDiagnostic
        .builder();
    Stopwatch stopwatch = Stopwatch.createStarted();

    //GoogleCredential is used to create a Google client with Drive. To send a file, you need to supply a File
    //and an InputStreamContent.
    GoogleCredential credential = new GoogleCredential().setAccessToken(
        executionContext.getAccessToken().get());
    Drive drive = new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
        credential).build();
    File file = new File().setName(getNameForDocument(document, integrationConfiguration));
    InputStreamContent mediaContent = new InputStreamContent("image/jpg", document.getInputStream());
    File responseFile;
    try {
      responseFile = drive.files().create(file, mediaContent).setFields("name, id, mimeType").execute();
    } catch (GoogleJsonResponseException e) {
      Map<String,Object> requestDiagnostics = getRequestDiagnostics(document, connectedSystemConfiguration,
          integrationConfiguration);
      return IntegrationExecutionUtils.handleException(e, diagnosticBuilder, requestDiagnostics, stopwatch);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
    Map<String,Object> requestDiagnostics = getRequestDiagnostics(document, connectedSystemConfiguration,
        integrationConfiguration);
    Map<String,Object> responseDiagnostics = IntegrationExecutionUtils.getResponseDiagnostics(responseFile);

    IntegrationDesignerDiagnostic diagnostic = diagnosticBuilder.addExecutionTimeDiagnostic(elapsed)
        .addRequestDiagnostic(requestDiagnostics)
        .addResponseDiagnostic(responseDiagnostics)
        .build();

    Map<String,Object> result = IntegrationExecutionUtils.getResult(responseFile);
    return IntegrationResponse.forSuccess(result).withDiagnostic(diagnostic).build();
  }

  private Map<String,Object> getRequestDiagnostics(
      Document document,
      SimpleConfiguration connectedSystemConfiguration,
      SimpleConfiguration integrationConfiguration) {
    Map<String,Object> requestDiagnostics = IntegrationExecutionUtils.getRequestDiagnostics(
        connectedSystemConfiguration);
    String documentName = getNameForDocument(document, integrationConfiguration);
    requestDiagnostics.put("Appian Document Name", documentName);
    requestDiagnostics.put("Appian Document Extension", document.getExtension());
    requestDiagnostics.put("Appian Document Size", document.getFileSize());
    requestDiagnostics.put("Appian Document Parent Folder ID", document.getParentFolderId());
    return requestDiagnostics;
  }

  //Returns a file name if it's provided, otherwise use's the document's Appian name
  private String getNameForDocument(Document document, SimpleConfiguration integrationConfiguration) {
    String documentNameFromUserInput = integrationConfiguration.getValue(NAME_KEY);
    if (documentNameFromUserInput != null && !documentNameFromUserInput.isEmpty()) {
      return documentNameFromUserInput;
    } else {
      return document.getFileName();
    }
  }
}

