package com.mycorp.googledrive.templates;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.oauth.SimpleOAuthConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.oauth.OAuthConfigurationData;

@TemplateId(name = "GoogleDriveSampleConnectedSystemTemplate", majorVersion = 2)
public class GoogleDriveSampleConnectedSystemTemplate extends SimpleOAuthConnectedSystemTemplate {

  public static final String CLIENT_ID_KEY = "clientId";
  public static final String CLIENT_SECRET_KEY = "clientSecret";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    return simpleConfiguration.setProperties(textProperty(CLIENT_ID_KEY)  //Need a text property for Client ID
            .label("Client Id")
            //Import customizable fields will need to be provided by the user upon import. Fields should
            //be marked import customizable when they are likely to change between environments. Encrypted
            //Fields must be import customizable
            .isImportCustomizable(true).build(),
        //Sensitive values should use an encryptedTextProperty. Encrypted Properties are masked in the UI
        //and import customizable
        encryptedTextProperty(CLIENT_SECRET_KEY).label("Client Secret").isImportCustomizable(true).build());
  }

  @Override
  protected OAuthConfigurationData getOAuthConfiguration(SimpleConfiguration simpleConfiguration) {
    //AuthUrl is provided in documentation at @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#step-2-send-a-request-to-googles-oauth-20-server">https://developers.google.com/identity/protocols/OAuth2InstalledApp#step-2-send-a-request-to-googles-oauth-20-server</a>
    //OAuth Scope URL is provided in documentation at @see <a href="https://developers.google.com/identity/protocols/googlescopes#drivev3">https://developers.google.com/identity/protocols/googlescopes#drivev3</a>
    //Token URL is provided in documentation at @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#exchange-authorization-code">https://developers.google.com/identity/protocols/OAuth2InstalledApp#exchange-authorization-code</a>
    return OAuthConfigurationData.builder()
        .authUrl("https://accounts.google.com/o/oauth2/v2/auth")
        .clientId(simpleConfiguration.getValue(CLIENT_ID_KEY))
        .clientSecret(simpleConfiguration.getValue(CLIENT_SECRET_KEY))
        .scope("https://www.googleapis.com/auth/drive")
        .tokenUrl("https://www.googleapis.com/oauth2/v4/token")
        .build();
  }
}
