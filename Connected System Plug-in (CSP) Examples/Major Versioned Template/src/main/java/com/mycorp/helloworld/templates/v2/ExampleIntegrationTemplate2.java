package com.mycorp.helloworld.templates.v2;

import static com.mycorp.helloworld.templates.v1.ExampleConnectedSystemTemplate.CS_PROP_KEY;

import java.util.HashMap;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;

@TemplateId(name="HelloWorldIntegrationTemplate")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class ExampleIntegrationTemplate2 extends SimpleIntegrationTemplate {

  public static final String INTEGRATION_PROP_KEY1 = "intProp1";
  public static final String INTEGRATION_PROP_KEY2 = "intProp2";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {
    return integrationConfiguration.setProperties(
        textProperty(INTEGRATION_PROP_KEY1)
            .label("Text Property")
            .isRequired(true)
            .build(),

        // Because we're adding a new REQUIRED property, this is a backwards INCOMPATIBLE change
        // Thus, we have to major version in order to not break existing integrations
        textProperty(INTEGRATION_PROP_KEY2)
            .label("Text Property 2")
            .isRequired(true)
            .build()
        );
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    String csValue = connectedSystemConfiguration.getValue(CS_PROP_KEY);
    String integrationValue = integrationConfiguration.getValue(INTEGRATION_PROP_KEY1).toString()
        + integrationConfiguration.getValue(INTEGRATION_PROP_KEY2).toString();
    Map<String,Object> result = new HashMap<>();

    result.put("value", csValue + integrationValue);

    return IntegrationResponse
        .forSuccess(result)
        .build();
  }
}
