package com.dataentry.forms.templates;

import com.appiancorp.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appiancorp.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;

@TemplateId(name="DynamicDataStructureConnectedSystemTemplate")
public class DynamicDataStructureConnectedSystemTemplate extends SimpleConnectedSystemTemplate {

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    //The connected system has no fields, therefore nothing to do
    return simpleConfiguration;
  }
}

