package com.mycorp.textdetection.templates;

import com.appiancorp.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appiancorp.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;

/**
 * This Connected System stores a secret value, the Google API key, which
 * the Client API will use to submit a request to the Google Text Detection API.
 */
@TemplateId(name = "TextDetectionConnectedSystemTemplate")
public class TextDetectionConnectedSystemTemplate extends SimpleConnectedSystemTemplate {

  public static final String API_KEY = "apiKey";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    return simpleConfiguration.setProperties(
        // Create an encryptedTextProperty to store the Google API key
        encryptedTextProperty(API_KEY)
            .label("API Key")
            .isImportCustomizable(true)
            .isRequired(true)
            .build()
    );
  }
}
