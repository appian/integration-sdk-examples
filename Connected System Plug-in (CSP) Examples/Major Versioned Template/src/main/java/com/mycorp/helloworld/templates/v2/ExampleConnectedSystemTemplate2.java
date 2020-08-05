package com.mycorp.helloworld.templates.v2;

import com.appian.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;

// IMPORTANT, the name has to stay the same between major versions
// Set the majorVersion as 2 since this is v2 of the template
@TemplateId(name="HelloWorldConnectedSystemTemplate", majorVersion = 2)
public class ExampleConnectedSystemTemplate2 extends SimpleConnectedSystemTemplate {

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
