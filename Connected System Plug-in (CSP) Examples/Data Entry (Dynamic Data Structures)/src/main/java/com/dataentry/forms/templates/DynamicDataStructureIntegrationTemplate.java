package com.dataentry.forms.templates;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyDescriptorBuilder;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.configuration.RefreshPolicy;
import com.appian.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@TemplateId(name = "DynamicDataStructureIntegrationTemplate")
public class DynamicDataStructureIntegrationTemplate extends SimpleIntegrationTemplate {

  /**
   * This is an example of an Integration Template that uses dynamic data structures. This integration
   * reads in data structures defined in a json file and create properties to represent the data structures.
   * This simulates retrieving data structures from an external system, such as Amazon Machine Learning models,
   * or data tables defined in a Salesforce system.
   *
   * In this example, it's a Data Entry plug-in that allows the user to fill an Account form or a Customer
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
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    //Here, we read the data structure from an example JSON file. In your plug-in, replace this with whatever
    // logic you need to retrieve the data structure from your external system.
    JsonNode externalDataTypes = getExternalDataTypes();
    TextPropertyDescriptor formDropdown = createFormDropdown(externalDataTypes);

    List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
    propertyDescriptors.add(formDropdown);

    List<PropertyDescriptor> formProperties = createFormPropertyDescriptorOnFormSelection(integrationConfiguration, externalDataTypes);
    propertyDescriptors.addAll(formProperties);

    return integrationConfiguration.setProperties(propertyDescriptors.toArray(new PropertyDescriptor[0]));
  }

  /**
   * Typically, you will want to send a HTTP request to an external system here, such as updating
   * a table in a Salesforce system.
   * In this integration, we simply return the user inputs to the result, request and response tabs.
   */
  @Override
  protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
    List<PropertyDescriptor> properties = integrationConfiguration.getProperties();

    long start = System.currentTimeMillis();
    Map<String, Object> requestMap = new HashMap<>();
    for (PropertyDescriptor p : properties) {
      requestMap.put(p.getKey(), integrationConfiguration.getValue(p.getKey()));
    }
    long end = System.currentTimeMillis();

    final long executionTime = end - start;
    final IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
        .addRequestDiagnostic(requestMap)
        .addExecutionTimeDiagnostic(executionTime)
        .build();
    return IntegrationResponse.forSuccess(requestMap).withDiagnostic(diagnostic).build();
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
  private List<PropertyDescriptor> createFormPropertyDescriptorOnFormSelection(SimpleConfiguration simpleConfiguration, JsonNode myCorpDataTypesAsJson){
    String formSelection = simpleConfiguration.getValue(FORM_DROPDOWN_KEY);
    if(formSelection != null) {
      for (JsonNode dataType : myCorpDataTypesAsJson) {
        if (formSelection.equals(dataType.get(NAME_KEY).asText())) {
          JsonNode selectedFormDataType = dataType;
          return createFormPropertyDescriptors(selectedFormDataType);
        }
      }
    }
    return Collections.emptyList();
  }

  /**
   * The json object contains the available data types and their structures.
   * JsonNode is an object that represents the data structures in the JSON file
   */
  private TextPropertyDescriptor createFormDropdown(JsonNode dataTypes) {
    Choice[] dataTypeChoices = createDataTypeChoices(dataTypes);
    return dropdownProperty(FORM_DROPDOWN_KEY, Arrays.asList(dataTypeChoices))
        .label(FORMS_DROPDOWN_LABEL)
        .instructionText(FormDropdownInstructionText)
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
        .map(dataTypeName -> Choice
            .builder()
            .name(dataTypeName)
            .value(dataTypeName)
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
      return textProperty("");
    } else if (SUPPORTED_PROPERTY_TYPE.INTEGER.name().equals(type)) {
      return integerProperty("");
    } else if (SUPPORTED_PROPERTY_TYPE.BOOLEAN.name().equals(type)) {
      return booleanProperty("");
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
