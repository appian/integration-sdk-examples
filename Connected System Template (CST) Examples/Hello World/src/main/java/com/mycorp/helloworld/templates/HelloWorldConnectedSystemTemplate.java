package com.mycorp.helloworld.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.textProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;

import com.appiancorp.connectedsystems.templateframework.sdk.ConnectedSystemTemplate;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;

@TemplateId(name="HelloWorldConnectedSystemTemplate")
public class HelloWorldConnectedSystemTemplate implements ConnectedSystemTemplate {

  public static final String CS_PROP_KEY = "csProp";

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor configurationDescriptor,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    if (configurationDescriptor != null) {
      // No dynamic behavior, so we can just return the config descriptor being passed to us
      return configurationDescriptor;
    }

    // Creates the root type. All properties in this type appear in the designer connected system UI
    LocalTypeDescriptor rootType = type()
        .name("RootCsType")
        .properties(
            // Can provide comma-separated properties
            textProperty()
                // Make sure you make public constants for all keys so that associated
                // integrations can easily access this field
                .key(CS_PROP_KEY)
                .label("Text Property")
                .description("This will be concatenated with the integration text property on execute")
                .build()
        )
        .build();

    // Need to generate default state for the root type to pass to the config descriptor builder
    // The state determines what fields actually appear in the UI
    // The type definition determines how it appears
    // See ConfigurationDescriptor for more information
    PropertyState rootState = new StateGenerator(rootType).generateDefaultState(rootType);

    return ConfigurationDescriptor.builder()
        .version(1)
        .withType(rootType)
        .withState(rootState)
        .build();
  }
}
