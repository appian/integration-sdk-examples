package com.dataentry.forms.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.textProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationTemplate;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyDescriptorBuilder;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.RefreshPolicy;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@TemplateId(name = "DynamicDataStructureIntegrationTemplate")
public class DynamicDataStructureIntegrationTemplate implements IntegrationTemplate{

  /**
   * This is an example of an Integration Template that uses dynamic data structures. It's a template that
   * reads in data structures defined in a json file and create properties to represent the data structures.
   * This simulates retrieving data structures from an external system, such as Amazon Machine Learning models,
   * or data tables defined in a Salesforce system.
   *
   * In this example, it's a Data Entry template that allows the user to fill an Account form or a Customer
   * form based on the selection of the form. The Integration Template will simply take in the entry values,
   * and return them to the result.
   */

  public static final String FORM_DROPDOWN_KEY = "formDropdown";
  public static final String NAME_KEY = "name";
  public static final String FIELDS_KEY = "fields";
  public static final String ID_KEY = "id";
  public static final String LABEL_KEY = "label";
  public static final String TYPE_KEY = "type";
  public static final String FormDropdownInstructionText = "Select a form to fill out";
  public static final String FORMS_DROPDOWN_LABEL = "My Corp Forms";

  enum SUPPORTED_PROPERTY_TYPE{
    TEXT, INTEGER, BOOLEAN
  }

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      PropertyPath updatedProperty,
      ExecutionContext executionContext) {
    //The Forms dropdown field is dynamic, meaning that when a change happens in the value of the dropdown,
    //the whole Integration Template will re-render in order to display the right set of fields for the selected
    //form. Therefore, we need to access the current state during configuration.
    PropertyState currentState = getInitialState(integrationConfigDescriptor);

    //This would be where the developer will make a call to an external system to retrieve
    //data structures. In this example, we will just read from a JSON file.
    JsonNode externalDataTypes = getExternalDataTypes();
    TextPropertyDescriptor formDropdown = createFormDropdown(externalDataTypes);

    List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
    propertyDescriptors.add(formDropdown);

    PropertyPath propertyPath = new PropertyPath("root", FORM_DROPDOWN_KEY);
    //If the selection in the dropdown changes, add the fields of the selected form to the properties
    if (propertyPath.equals(updatedProperty)) {
      List<PropertyDescriptor> formProperties = createFormPropertyDescriptorOnFormSelection(currentState, externalDataTypes);
      propertyDescriptors.addAll(formProperties);
    }

    LocalTypeDescriptor localType = DomainSpecificLanguage.type()
        .name("MyComplexDataType")
        .properties(propertyDescriptors)
        .build();

    PropertyState propertyState = generatePropertyState(integrationConfigDescriptor, localType);
    return ConfigurationDescriptor.builder()
        .withState(propertyState)
        .withTypes(localType)
        .version(1)
        .build();
  }

  /**
   * Typically, you will want to send a HTTP request to an external system here, such as updating
   * a table in a Salesforce system.
   * In this template, it will simply return the user inputs to the result, request and response tabs.
   */
  @Override
  public IntegrationResponse execute(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      ExecutionContext executionContext) {
    PropertyState rootState = integrationConfigDescriptor.getRootState();
    Map<String, Object> rootStateValues = (Map<String, Object>)rootState.getValue();

    long start = System.currentTimeMillis();
    Map<String, String> requestMap = new HashMap<>();
    for (Map.Entry<String, Object> entry : rootStateValues.entrySet()) {
      requestMap.put(entry.getKey(), ((PropertyState)entry.getValue()).getValue().toString());
    }
    long end = System.currentTimeMillis();

    final long executionTime = end - start;
    final IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
        .addRequestDiagnostic(rootStateValues)
        .addResponseDiagnostic(requestMap)
        .addExecutionTimeDiagnostic(executionTime)
        .build();
    return IntegrationResponse.forSuccess(rootStateValues).withDiagnostic(diagnostic).build();
  }

  /**
   * Since the Form dropdown field is dynamic, we need to generate the property state using the the existing
   * state which contains the new selection of the form. This will make sure that the new selection of the form
   * still shows as the selection after the Integration Template re-renders.
   */
  private PropertyState generatePropertyState(
      ConfigurationDescriptor integrationConfigDescriptor,
      LocalTypeDescriptor localType) {
    StateGenerator stateGenerator = new StateGenerator(localType);
    PropertyState propertyState;
    if (integrationConfigDescriptor == null || integrationConfigDescriptor.getState().isEmpty()) {
      propertyState = stateGenerator.generateDefaultState(localType);
    } else {
      propertyState = stateGenerator.generateFromExistingState(localType,
          integrationConfigDescriptor.getRootState(), new PropertyPath());
    }
    return propertyState;
  }

  /**
   * Read the data structure json file into a JsonNode
   */
  private JsonNode getExternalDataTypes() {
    String myCorpFormDataTypesInString = readJsonFile();
    try {
      JsonNode dataTypesAsJson = new ObjectMapper().readTree(myCorpFormDataTypesInString);
      return dataTypesAsJson;
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * The json file needs to be in the resources directory in order to be read properly
   */
  private static String readJsonFile() {
    try (InputStream input = DynamicDataStructureIntegrationTemplate.class.getClassLoader()
        .getResourceAsStream("com/dataentry/forms/templates/ExternalDataTypes.json")) {
      return IOUtils.toString(input, "utf-8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Return a list of PropertyDescriptor depending on the form selection. For example, when user selects "account"
   * form, this reads in the "account" table data structures and create PropertyDescriptor for "Account ID" and
   * "Account Holder"
   */
  private List<PropertyDescriptor> createFormPropertyDescriptorOnFormSelection(PropertyState state, JsonNode myCorpDataTypesAsJson){
    String formSelection = (String)state.getValue(new PropertyPath(FORM_DROPDOWN_KEY));
    for (JsonNode dataType : myCorpDataTypesAsJson) {
      if (formSelection.equals(dataType.get(NAME_KEY).asText())) {
        JsonNode selectedFormDataType = dataType;
        return createFormPropertyDescriptors(selectedFormDataType);
      }
    }
    return Collections.emptyList();
  }

  /**
   * This method will return the current state of the Integration Template.
   */
  private PropertyState getInitialState(ConfigurationDescriptor integrationConfigDescriptor) {
    if (integrationConfigDescriptor == null || integrationConfigDescriptor.getState().isEmpty()) {
      return null;
    } else {
      return integrationConfigDescriptor.getRootState();
    }
  }

  /**
   * The json object contains the available data types and their structures.
   * JsonNode is an object that represents the data structures in the JSON file
   */
  private TextPropertyDescriptor createFormDropdown(JsonNode dataTypes) {
    Choice[] dataTypeChoices = createDataTypeChoices(dataTypes);
    return textProperty().key(FORM_DROPDOWN_KEY)
        .label(FORMS_DROPDOWN_LABEL)
        .instructionText(FormDropdownInstructionText)
        .choices(dataTypeChoices)
        //Important: This triggers the Integration Template to re-render when the choice selection changes
        .refresh(RefreshPolicy.ALWAYS)
        .isRequired(true)
        .build();
  }

  private Choice[] createDataTypeChoices(JsonNode dataTypesAsJson) {
    List<String> dataTypeNames = new ArrayList<>();
    for (JsonNode dataType : dataTypesAsJson) {
      String name = dataType.get(NAME_KEY).asText();
      dataTypeNames.add(name);
    }
    return dataTypeNames.stream()
        .map(dataTypeName -> DomainSpecificLanguage
            .choice()
            .name(dataTypeName)
            .textValue(dataTypeName)
            .build()).toArray(Choice[]::new);
  }

  private List<PropertyDescriptor> createFormPropertyDescriptors(JsonNode formDataType) {
    JsonNode properties = formDataType.get(FIELDS_KEY);
    List<PropertyDescriptor> propList = new ArrayList<>();
    for (JsonNode prop : properties) {
      String key = prop.get(ID_KEY).asText();
      String label = prop.get(LABEL_KEY).asText();
      String type = prop.get(TYPE_KEY).asText();
      PropertyDescriptorBuilder propertyDescriptorBuilder = parsePropertyDescriptorType(type);
      PropertyDescriptor propertyDescriptor = createPropertyDescriptor(key, label, propertyDescriptorBuilder);
      propList.add(propertyDescriptor);
    }
    return propList;
  }

  private PropertyDescriptorBuilder parsePropertyDescriptorType(String type) {
    if (SUPPORTED_PROPERTY_TYPE.TEXT.name().equals(type)) {
      return DomainSpecificLanguage.textProperty();
    } else if (SUPPORTED_PROPERTY_TYPE.INTEGER.name().equals(type)) {
      return DomainSpecificLanguage.integerProperty();
    } else if (SUPPORTED_PROPERTY_TYPE.BOOLEAN.name().equals(type)) {
      return DomainSpecificLanguage.booleanProperty();
    } else {
      throw new RuntimeException("Unsupported Property Type");
    }
  }

  private PropertyDescriptor createPropertyDescriptor(String key, String label, PropertyDescriptorBuilder builder) {
    return builder
        .key(key)
        .label(label)
        .build();
  }
}
