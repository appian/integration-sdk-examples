package com.example.errorhandling.templates;

import com.appiancorp.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appiancorp.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;

@TemplateId(name="ErrorHandlingConnectedSystemTemplate")
public class ErrorHandlingConnectedSystemTemplate extends SimpleConnectedSystemTemplate {
  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    //The connected system has no fields, therefore nothing to do here
    return simpleConfiguration;
  }
}
