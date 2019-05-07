package com.mycorp.dropdowndiffsapp.templates;

import com.appian.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appian.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;

@TemplateId(name="DropdownDiffsConnectedSystemTemplate")
public class DropdownDiffsConnectedSystemTemplate extends SimpleConnectedSystemTemplate {

  public static final String CS_PROP_KEY = "csProp";

  DropdownDiffsConnectedSystemTemplate() {

  }

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {

    return simpleConfiguration.setProperties(
        // Make sure you make public constants for all keys so that associated
        // integrations can easily access this field
        localTypeProperty(LocalTypeDescriptor.builder()
            .name("localPropertyCS")
            .properties(
                textProperty("localTypeDropdown_cs").label("Local Type Dropdown")
                  .description("a;lsdkfjas;ldkfjasdf")
                  .choices(new Choice.ChoiceBuilder().name("Local Type Dropdown Choice 1")
                      .value("local_type_dropdown_choice_1_cs_changed")
                      .build(), new Choice.ChoiceBuilder().name("Local Type Dropdown Choice 2")
                      .value("local_type_dropdown_choice_2_cs_changed")
                      .build())
                  .build(),
                textProperty("localTypeTextBox").label("Local Type Text Box")
                  .description("This will be concatenated with the integration text property on execute")
                  .build())
            .build(), "localTypeCS").build(),
        textProperty("rootPropertyDropdown_cs").label("Root Property Dropdown")
            .choices(new Choice.ChoiceBuilder().name("Root Property Choice 1")
                .value("root_property_choice_1_cs_changed")
                .build(), new Choice.ChoiceBuilder().name("Root Property Choice 2")
                .value("root_property_choice_2_cs_changed")
                .build())
            .description("This will be concatenated with the integration text property on execute")
            .build(),
        textProperty("rootPropertyTextBox_cs").label("Root Property Text Box")
            .description("This will be concatenated with the integration text property on execute")
            .build());
  }
}
