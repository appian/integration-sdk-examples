package com.mycorp.helloworld.templates;

import static com.mycorp.helloworld.templates.HelloWorldConnectedSystemTemplate.CS_PROP_KEY;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.DisplayHint;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.DocumentLocationPropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.DocumentMetadataAndInfo;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.configuration.RefreshPolicy;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.connectedsystems.templateframework.sdk.streaming.ContentService;

// Must provide an integration id. This value need only be unique for this connected system
@TemplateId(name="HelloWorldIntegrationTemplate")
// Set template type to READ since this integration does not have side effects
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class HelloWorldIntegrationTemplate extends SimpleIntegrationTemplate {

  public static final String INTEGRATION_PROP_KEY = "intProp";
  public static final String SIMPLE_DOC_LOCATION_KEY = "SIMPLE_DOC_LOCATION_KEY";

  private static final String tmpDirectory = "/var/tmp/";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration.setProperties(
        // Make sure you make constants for all keys so that you can easily
        // access the values during execution
        DocumentLocationPropertyDescriptor.builder()
            .key(SIMPLE_DOC_LOCATION_KEY)
            .label("Document Location")
            .description("document description")
            .instructionText("document instructions")
            .placeholder("document placeholder")
            .refresh(RefreshPolicy.ALWAYS)
            .displayHint(DisplayHint.NORMAL)
            .isRequired(true)
            .isExpressionable(true)
            .isReadOnly(false)
            .isHidden(false)
            .build(),
        textProperty(INTEGRATION_PROP_KEY).label("Text Property")
            .isRequired(true)
            .description("This will be concatenated with the connected system text property on execute")
            .build());
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    Map<String,Object> requestDiagnostic = new HashMap<>();
    String csValue = connectedSystemConfiguration.getValue(CS_PROP_KEY);
    requestDiagnostic.put("csValue", csValue);
    String integrationValue = integrationConfiguration.getValue(INTEGRATION_PROP_KEY);
    requestDiagnostic.put("integrationValue", integrationValue);
    Map<String,Object> result = new HashMap<>();

    // Important for debugging to capture the amount of time it takes to interact
    // with the external system. Since this integration doesn't interact
    // with an external system, we'll just log the calculation time of concatenating the strings
    final long start = System.currentTimeMillis();
    result.put("hello", "world");
    result.put("concat", csValue + integrationValue);
    Long folderId = integrationConfiguration.getValue(SIMPLE_DOC_LOCATION_KEY);

    InputStream stream = new ByteArrayInputStream("asdf".getBytes());
    long fileSize = "asdf".getBytes().length;
    DocumentMetadataAndInfo documentToBeSaved = DocumentMetadataAndInfo.builder()
        .documentLocation(folderId)
        .inputStream(stream)
        .fileName("asdf")
        .fileSize(fileSize)
        .build();

    ContentService contentService = executionContext.getContentService();
    Set<Document> documents = new HashSet<>();
    documents.add(simpleUploadRetryLogic(documentToBeSaved, contentService, 2));
    documents = documents
        .stream()
        .filter(Objects::nonNull).collect(Collectors.toSet());
    if (documents.isEmpty()) {
      return IntegrationResponse.forError(
          IntegrationError.builder().title("Couldn't upload any documents").build()).build();
    }
    result.put("savedDocuments", documents);
    final long end = System.currentTimeMillis();

    final long executionTime = end - start;
    final IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(executionTime)
        .addRequestDiagnostic(requestDiagnostic)
        .build();

    return IntegrationResponse
        .forSuccess(result)
        .withDiagnostic(diagnostic)
        .build();
  }

  private Document simpleUploadRetryLogic(
      DocumentMetadataAndInfo documentToBeSaved, ContentService contentService, int retry) {
    if (retry == 0) {
      return null;
    }
    try {
      return contentService.write(documentToBeSaved);
    } catch (Exception e) {
      return simpleUploadRetryLogic(documentToBeSaved, contentService, retry - 1);
    }
  }
}
