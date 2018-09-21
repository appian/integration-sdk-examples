package com.mycorp.textdetection.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.encryptedTextProperty;

import com.appiancorp.connectedsystems.templateframework.sdk.ConnectedSystemTemplate;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;

/**
 * This Connected System stores a secret value, the Google API key, which
 * the Client API will use to submit a request to the Google Text Detection API.
 */
@TemplateId(name = "TextDetectionConnectedSystemTemplate")
public class TextDetectionConnectedSystemTemplate implements ConnectedSystemTemplate {

  public static final String API_KEY = "apiKey";

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor configDescriptor,
      PropertyPath updatedProperty,
      ExecutionContext executionContext) {
    LocalTypeDescriptor localTypeDescriptor = createConnectedSystemRootTypeDescriptor();

    PropertyState propertyState;

    StateGenerator stateGenerator = new StateGenerator(localTypeDescriptor);

    // Generates a default state on the initial call to the Connected System
    if (configDescriptor == null || configDescriptor.getState().isEmpty()) {
      propertyState = stateGenerator.generateDefaultState(localTypeDescriptor);
    } else {
      PropertyState rootState = configDescriptor.getRootState();
      propertyState = stateGenerator.generateFromExistingState(localTypeDescriptor,
          rootState, new PropertyPath());
    }

    return ConfigurationDescriptor.builder()
        .withType(localTypeDescriptor)
        .withState(propertyState)
        .version(1)
        .build();
  }

  private LocalTypeDescriptor createConnectedSystemRootTypeDescriptor() {
    // Create an encryptedTextProperty to store the Google API key
    return LocalTypeDescriptor.builder()
        .name("GoogleVisionCsConfiguration")
        .properties(
            encryptedTextProperty().key(API_KEY)
                .label("API Key")
                .isImportCustomizable(true)
                .isRequired(true)
                .build())
        .build();
  }

}
