package com.mycorp.github.templates;

import com.appiancorp.connectedsystems.templateframework.sdk.ConnectedSystemTemplate;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;

@TemplateId(name = "github")
public class GithubConnectedSystemTemplate implements ConnectedSystemTemplate {
  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor configDescriptor,
      PropertyPath updatedProperty,
      ExecutionContext executionContext) {
    if(configDescriptor != null) {
      return configDescriptor;
    }
    GithubConnectedSystemTypeFactory typeFactory = new GithubConnectedSystemTypeFactory();
    LocalTypeDescriptor localTypeDescriptor = typeFactory.getCSType();
    StateGenerator stateGenerator = new StateGenerator(localTypeDescriptor);
    PropertyState propertyState = stateGenerator.generateDefaultState(localTypeDescriptor);

    return ConfigurationDescriptor.builder()
        .withState(propertyState)
        .withType(localTypeDescriptor)
        .build();
  }
}
