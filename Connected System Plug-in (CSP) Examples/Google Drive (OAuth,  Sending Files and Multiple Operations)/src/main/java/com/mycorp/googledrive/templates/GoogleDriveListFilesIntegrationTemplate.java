package com.mycorp.googledrive.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Stopwatch;

@TemplateId(name = "GoogleDriveListFilesIntegrationTemplate")
public class GoogleDriveListFilesIntegrationTemplate extends SimpleIntegrationTemplate {
  private static final String FOLDER_ID_KEY = "folderId";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      PropertyPath updatedProperty,
      ExecutionContext executionContext) {
    return integrationConfiguration.setProperties(textProperty(FOLDER_ID_KEY).label("Folder Id")
        .instructionText("If left blank, it will list the files in the root folder. Otherwise it will list the " +
            "children files in the specified folder")
        .build());
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    String folderId = integrationConfiguration.getValue(FOLDER_ID_KEY);

    IntegrationDesignerDiagnostic.IntegrationDesignerDiagnosticBuilder diagnosticBuilder = IntegrationDesignerDiagnostic
        .builder();
    Stopwatch stopwatch = Stopwatch.createStarted();

    GoogleCredential credential = new GoogleCredential().setAccessToken(
        executionContext.getAccessToken().get());
    Drive drive = new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
        credential).build();

    ArrayList<File> queriedFiles = new ArrayList<File>();
    Drive.Files.List request;
    try {
      // This request is only being performed once and does not deal with paging results, which means that
      // the result list might not be complete
      request = drive.files().list();
      if (folderId == null || folderId.isEmpty()) {
        folderId = "root";
      }
      request.setQ("'" + folderId + "' in parents");
      try {
        FileList files = request.execute();
        queriedFiles.addAll(files.getFiles());
      } catch (GoogleJsonResponseException e) {
        Map<String,Object> requestDiagnostics = getRequestDiagnostics(folderId, connectedSystemConfiguration);
        return IntegrationExecutionUtils.handleException(e, diagnosticBuilder, requestDiagnostics, stopwatch);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    HashMap<String,Object> resultMap = new HashMap<>();
    resultMap.put("files", queriedFiles);
    Map<String,Object> requestDiagnostics = getRequestDiagnostics(
        folderId, connectedSystemConfiguration);

    Map<String,Object> diagnosticResponse = new HashMap<>();
    diagnosticResponse.put("Number of files", queriedFiles.size());
    IntegrationDesignerDiagnostic diagnostic = diagnosticBuilder
        .addExecutionTimeDiagnostic(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS))
        .addRequestDiagnostic(requestDiagnostics)
        .addResponseDiagnostic(diagnosticResponse)
        .build();
    return IntegrationResponse.forSuccess(resultMap).withDiagnostic(diagnostic).build();
  }

  private Map<String,Object> getRequestDiagnostics(
      String folderId,
      SimpleConfiguration connectedSystemConfiguration) {
    Map<String,Object> requestDiagnostics = IntegrationExecutionUtils.getRequestDiagnostics(
        connectedSystemConfiguration);
    requestDiagnostics.put("Folder ID", folderId);
    return requestDiagnostics;
  }
}
