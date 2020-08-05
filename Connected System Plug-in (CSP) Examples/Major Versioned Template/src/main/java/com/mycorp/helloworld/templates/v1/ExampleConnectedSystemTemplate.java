package com.mycorp.helloworld.templates.v1;

import com.appian.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;

// IMPORTANT, the name has to stay the same between major versions
// This annotation doesn't require an explicit majorVersion element since the default value is 1.
@TemplateId(name="HelloWorldConnectedSystemTemplate")
public class ExampleConnectedSystemTemplate extends SimpleConnectedSystemTemplate {

  public static final String CS_PROP_KEY = "csProp";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {

    return simpleConfiguration.setProperties(
        textProperty(CS_PROP_KEY)
            .label("Text Property")
            .build()
    );
  }
}
