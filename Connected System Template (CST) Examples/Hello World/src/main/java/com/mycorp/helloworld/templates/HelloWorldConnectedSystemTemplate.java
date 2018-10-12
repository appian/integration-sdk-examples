package com.mycorp.helloworld.templates;

import com.appiancorp.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appiancorp.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;

@TemplateId(name="HelloWorldConnectedSystemTemplate")
public class HelloWorldConnectedSystemTemplate extends SimpleConnectedSystemTemplate {

  public static final String CS_PROP_KEY = "csProp";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {

    return simpleConfiguration.setProperties(
        // Make sure you make public constants for all keys so that associated
        // integrations can easily access this field
        textProperty(CS_PROP_KEY)
            .label("Text Property")
            .description("This will be concatenated with the integration text property on execute")
            .build()
    );
  }
}
