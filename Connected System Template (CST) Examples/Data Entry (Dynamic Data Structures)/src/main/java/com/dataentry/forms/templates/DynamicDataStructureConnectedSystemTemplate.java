package com.dataentry.forms.templates;

import com.appian.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;

@TemplateId(name="DynamicDataStructureConnectedSystemTemplate")
public class DynamicDataStructureConnectedSystemTemplate extends SimpleConnectedSystemTemplate {

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    //The connected system has no fields, therefore nothing to do
    return simpleConfiguration;
  }
}

