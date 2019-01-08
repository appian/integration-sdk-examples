package com.mycorp.googledrive.version3;

import com.appian.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.oauth.SimpleOAuthConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.oauth.OAuthConfigurationData;

@TemplateId(name = "GoogleDriveSampleConnectedSystemTemplate", majorVersion = 3)
public class GoogleDriveSampleConnectedSystemTemplate extends SimpleConnectedSystemTemplate {

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
}
