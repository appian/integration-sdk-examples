package com.mycorp.helloworld.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.textProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;
import static com.mycorp.helloworld.templates.HelloWorldConnectedSystemTemplate.CS_PROP_KEY;

import java.util.HashMap;
import java.util.Map;

import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;
import com.appiancorp.connectedsystems.templateframework.sdk.diagnostic.IntegrationDesignerDiagnostic;
import com.appiancorp.connectedsystems.templateframework.sdk.service.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.service.IntegrationResponse;
import com.appiancorp.connectedsystems.templateframework.sdk.service.annotation.IntegrationTemplateRequestPolicy;
import com.appiancorp.connectedsystems.templateframework.sdk.service.annotation.IntegrationTemplateType;
import com.appiancorp.connectedsystems.templateframework.sdk.service.annotation.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.service.v2.IntegrationTemplate;

// Must provide an integration id. This value need only be unique for this connected system
@TemplateId("HelloWorldIntegrationTemplate")
// Set template type to READ since this integration does not have side effects
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class HelloWorldIntegrationTemplate implements IntegrationTemplate {

  public static final String INTEGRATION_PROP_KEY = "intProp";

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      PropertyPath updatedProperty,
      ExecutionContext executionContext) {
    if (integrationConfigDescriptor != null) {
      // No dynamic behavior, so we can just return the config descriptor being passed to us
      return integrationConfigDescriptor;
    }

    // Creates the root type. All properties in this type appear in the designer integration UI
    LocalTypeDescriptor rootType = type()
        .name("RootIntegrationType")
        .properties(
            // Can provide comma-separated properties
            textProperty()
                // Make sure you make constants for all keys so that you can easily
                // access the values during execution
                .key(INTEGRATION_PROP_KEY)
                .label("Text Property")
                .description("This will be concatenated with the connected system text property on execute")
                .build()
        )
        .build();

    // Need to generate default state for the root type to pass to the config descriptor builder
    // The state determines what fields actually appear in the UI.
    // The type definition determines how it appears
    // See ConfigurationDescriptor for more information
    PropertyState rootState = new StateGenerator(rootType).generateDefaultState(rootType);

    return ConfigurationDescriptor.builder()
        .version(1)
        .withType(rootType)
        .withState(rootState)
        .build();
  }

  @Override
  public IntegrationResponse execute(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      ExecutionContext executionContext) {

    // Diagnostics are used by designers to debug requests. Add all non-sensitive values
    // used for integrating to the request diagnostics map. Valid values are primitives, lists, and maps.
    Map<String,Object> requestDiagnostic = new HashMap<>();
    String csValue = (String)connectedSystemConfigDescriptor.getRootState()
        .getValue(new PropertyPath(CS_PROP_KEY));
    requestDiagnostic.put("csValue", csValue);

    String integrationValue = (String)integrationConfigDescriptor.getState()
        .get("root")
        .getValue(new PropertyPath(INTEGRATION_PROP_KEY));
    requestDiagnostic.put("integrationValue", integrationValue);

    Map<String,Object> result = new HashMap<>();

    // Important for debugging to capture the amount of time it takes to interact
    // with the external system. Since this integration doesn't interact
    // with an external system, we'll just log the calculation time of concatenating the strings
    final long start = System.currentTimeMillis();
    result.put("hello", "world");
    result.put("concat", csValue + integrationValue);
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
}
