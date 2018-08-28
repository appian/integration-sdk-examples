package com.mycorp.helloworld.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.encryptedTextProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.textProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;

import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;
import com.appiancorp.connectedsystems.templateframework.sdk.oauth.OAuthConfigurationData;
import com.appiancorp.connectedsystems.templateframework.sdk.oauth.OAuthConnectedSystemTemplate;

@TemplateId(name="GoogleDriveSampleConnectedSystemTemplate")
public class GoogleDriveSampleConnectedSystemTemplate implements OAuthConnectedSystemTemplate {

  public static final String CLIENT_ID_KEY = "clientId";
  public static final String CLIENT_SECRET_KEY = "clientSecret";
  public static final String ROOT_CS_TYPE = "RootCsType";

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor configurationDescriptor,
      PropertyPath propertyPath,
      ExecutionContext executionContext) {
    if (configurationDescriptor != null) {
      //No dynamic behavior for this connected system. The only modifications come from user input
      return configurationDescriptor;
    }

    LocalTypeDescriptor rootType = type()
        .name(ROOT_CS_TYPE)
        .properties(
            textProperty()  //Need a text property for Client ID
                .key(CLIENT_ID_KEY)
                .label("Client Id")
                //Import customizable fields will need to be provided by the user upon import. Fields should
                //be marked import customizable when they are likely to change between environments. Encrypted
                //Fields must be import customizable
                .isImportCustomizable(true)
                .build(),
            //Sensitive values should use an encryptedTextProperty. Encrypted Properties are masked in the UI
            //and import customizable
            encryptedTextProperty()
                .key(CLIENT_SECRET_KEY)
                .label("Client Secret")
                .isImportCustomizable(true)
                .build()
        )
        .build();

    //Generates the initial PropertyState object for the given local type
    PropertyState rootState = new StateGenerator(rootType).generateDefaultState(rootType);
    return ConfigurationDescriptor.builder()
        .version(1)
        .withType(rootType)
        .withState(rootState)
        .build();
  }

  @Override
  public OAuthConfigurationData getOAuthConfiguration(
      ConfigurationDescriptor configurationDescriptor) {
    PropertyState rootState = configurationDescriptor.getRootState();
    //AuthUrl is provided in documentation at @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#step-2-send-a-request-to-googles-oauth-20-server">https://developers.google.com/identity/protocols/OAuth2InstalledApp#step-2-send-a-request-to-googles-oauth-20-server</a>
    //OAuth Scope URL is provided in documentation at @see <a href="https://developers.google.com/identity/protocols/googlescopes#drivev3">https://developers.google.com/identity/protocols/googlescopes#drivev3</a>
    //Token URL is provided in documentation at @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#exchange-authorization-code">https://developers.google.com/identity/protocols/OAuth2InstalledApp#exchange-authorization-code</a>
    return OAuthConfigurationData.builder()
        .authUrl("https://accounts.google.com/o/oauth2/v2/auth")
        .clientId((String)rootState.getValue(new PropertyPath(CLIENT_ID_KEY)))
        .clientSecret((String)rootState.getValue(new PropertyPath(CLIENT_SECRET_KEY)))
        .scope("https://www.googleapis.com/auth/drive")
        .tokenUrl("https://www.googleapis.com/oauth2/v4/token")
        .build();
  }
}
