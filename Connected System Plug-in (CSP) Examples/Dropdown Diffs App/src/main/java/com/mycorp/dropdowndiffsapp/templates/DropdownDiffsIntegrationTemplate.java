package com.mycorp.dropdowndiffsapp.templates;

import static com.mycorp.dropdowndiffsapp.templates.DropdownDiffsConnectedSystemTemplate.CS_PROP_KEY;

import java.util.HashMap;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appian.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;

// Must provide an integration id. This value need only be unique for this connected system
@TemplateId(name="DropdownDiffsIntegrationTemplate")
// Set template type to READ since this integration does not have side effects
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class DropdownDiffsIntegrationTemplate extends SimpleIntegrationTemplate {

  public static final String INTEGRATION_PROP_KEY = "intProp";

  DropdownDiffsIntegrationTemplate() {

  }

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    return integrationConfiguration.setProperties(
        // Make sure you make constants for all keys so that you can easily
        // access the values during execution
        localTypeProperty(LocalTypeDescriptor.builder()
            .name("localPropertyInt")
            .properties(
                textProperty("localTypeDropdown_int").label("Local Type Dropdown")
                  .description("a;lsdkfjas;ldkfjasdf")
                  .choices(new Choice.ChoiceBuilder().name("Local Type Dropdown Choice 1")
                      .value("local_type_dropdown_choice_1_int_changed")
                      .build(), new Choice.ChoiceBuilder().name("Local Type Dropdown Choice 2")
                      .value("local_type_dropdown_choice_2_int_changed")
                      .build())
                  .build(),
                textProperty("localTypeTextBox_int").label("Local Type Text Box")
                  .description("This will be concatenated with the integration text property on execute")
                  .build())
            .build(), "localTypeInt").build(),
        textProperty("rootPropertyDropdown_int").label("Root Property Dropdown")
            .choices(new Choice.ChoiceBuilder().name("Root Property Choice 1")
                .value("root_property_choice_1_int_changed")
                .build(), new Choice.ChoiceBuilder().name("Root Property Choice 2")
                .value("root_property_choice_2_int_changed")
                .build())
            .description("This will be concatenated with the integration text property on execute")
            .build(),
        textProperty("rootPropertyTextBox_int").label("Root Property Text Box")
            .description("This will be concatenated with the integration text property on execute")
            .build());
  }

  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    Map<String,Object> requestDiagnostic = new HashMap<>();
    String csValue = connectedSystemConfiguration.getValue(CS_PROP_KEY);
    requestDiagnostic.put("csValue", csValue);
    String integrationValue = integrationConfiguration.getValue(INTEGRATION_PROP_KEY);
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
