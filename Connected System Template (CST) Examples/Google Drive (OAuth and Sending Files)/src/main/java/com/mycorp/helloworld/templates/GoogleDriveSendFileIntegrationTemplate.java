package com.mycorp.helloworld.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.documentProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;
import static com.mycorp.helloworld.templates.GoogleDriveSampleConnectedSystemTemplate.CLIENT_ID_KEY;
import static com.mycorp.helloworld.templates.GoogleDriveSampleConnectedSystemTemplate.CLIENT_SECRET_KEY;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationError;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationTemplate;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.Document;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;
import com.appiancorp.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appiancorp.connectedsystems.templateframework.sdk.oauth.ExpiredTokenException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

@TemplateId(name="GoogleDriveSendFileIntegrationTemplate")
public class GoogleDriveSendFileIntegrationTemplate implements IntegrationTemplate {

  public static final String FILE_KEY = "fileId";

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      PropertyPath updatedProperty,
      ExecutionContext executionContext) {
    if (integrationConfigDescriptor != null) {
      //No dynamic behavior for this integration. The only modifications come from user input
      return integrationConfigDescriptor;
    }

    LocalTypeDescriptor rootType = type()
        .name("RootIntegrationType")
        .properties(
            //Document Property allows the user to select a document in Appian to send to Google Drive
            documentProperty()
                .key(FILE_KEY)
                .label("File")
                .isRequired(true)
                .build()
        )
        .build();

    PropertyState rootState = new StateGenerator(rootType).generateDefaultState(rootType);

    return ConfigurationDescriptor.builder()
        .version(1)
        .withType(rootType)
        .withState(rootState)
        .build();
  }

  @Override
  public IntegrationResponse execute(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      ExecutionContext executionContext) {
    Map<String,Object> requestDiagnostic = new HashMap<>();

    //State objects contain the user configured values in the connected system and integration
    PropertyState connectedSystemState = connectedSystemConfigDescriptor.getRootState();
    String clientId = (String)connectedSystemState.getValue(new PropertyPath(CLIENT_ID_KEY));
    String clientSecret = (String)connectedSystemState.getValue(new PropertyPath(CLIENT_SECRET_KEY));

    //Request Diagnostic values should be shown on the Request tab on Appian Integration Designer Interface,
    //which will be visible to designers. Only add to diagnostics values that you wish the designer to see.
    requestDiagnostic.put(CLIENT_ID_KEY, clientId);
    //For sensitive values, mask it so that it won't be visible to designers
    requestDiagnostic.put(CLIENT_SECRET_KEY, "******************");

    PropertyState rootState = integrationConfigDescriptor.getRootState();
    Document document = (Document)rootState.getValue(new PropertyPath(FILE_KEY));

    requestDiagnostic.put("Appian Document Name", document.getFileName());
    requestDiagnostic.put("Appian Document Extension", document.getExtension());
    requestDiagnostic.put("Appian Document Size", document.getFileSize());
    requestDiagnostic.put("Appian Document Parent Folder ID", document.getParentFolderId());

    File file = new File().setName(document.getFileName());

    InputStreamContent mediaContent = new InputStreamContent("image/jpg", document.getInputStream());

    Map<String,Object> result = new HashMap<>();
    Map<String, String> responseDiagnostic = new HashMap<>();

    GoogleCredential credential = new GoogleCredential().setAccessToken(executionContext.getAccessToken().get());
    Drive drive = new Drive.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).build();
    IntegrationDesignerDiagnostic.IntegrationDesignerDiagnosticBuilder diagnosticBuilder =  IntegrationDesignerDiagnostic.builder();

    final long start = System.currentTimeMillis();
    long end;
    try {
      File responseFile = drive
          .files()
          .create(file, mediaContent)
          .setFields("name, id, mimeType")
          .execute();
      result.put("Name", responseFile.getName());
      result.put("File ID", responseFile.getId());
      result.put("MIME Type", responseFile.getId());

      responseDiagnostic.put("Name", responseFile.getName());
      responseDiagnostic.put("File ID", responseFile.getId());
      responseDiagnostic.put("MIME Type", responseFile.getMimeType());
      end = System.currentTimeMillis();
    } catch (GoogleJsonResponseException e) {
      if (e.getStatusCode() == 401) {
        //Google returns a 401 exception if your credential is not authorized or expired. Throw a
        //ExpiredTokenException when this happens, Appian will try to refresh the token for up to
        //3 times for you.
        throw new ExpiredTokenException();
      }
      end = System.currentTimeMillis();
      IntegrationDesignerDiagnostic diagnostic = diagnosticBuilder.addExecutionTimeDiagnostic(end - start)
          .addRequestDiagnostic(requestDiagnostic).build();

      IntegrationError error =  IntegrationError.builder()
          .title("Google Exception Status: " + e.getStatusMessage())
          .message(e.getMessage())
          .build();

      return IntegrationResponse
          .forError(error)
          .withDiagnostic(diagnostic)
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    IntegrationDesignerDiagnostic diagnostic = diagnosticBuilder
        .addExecutionTimeDiagnostic(end - start)
        .addRequestDiagnostic(requestDiagnostic)
        .addResponseDiagnostic(responseDiagnostic)
        .build();

    return IntegrationResponse
        .forSuccess(result)
        .withDiagnostic(diagnostic)
        .build();
  }
}

