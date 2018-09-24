package com.mycorp.github.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.choice;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.encryptedTextProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.textProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;

import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;

public class GithubConnectedSystemTypeFactory {

  public static final String ACCESS_KEY = "accessKey";
  public static final String AUTH_TOKEN = "authToken";
  public static final String GITHUB_CREDENTIAL_TYPE_NAME = "GITHUB_OAUTH";

  public LocalTypeDescriptor getCSType() {
    return type()
        .name(GITHUB_CREDENTIAL_TYPE_NAME)
        .properties(
            encryptedTextProperty()
                .key(AUTH_TOKEN)
                .label("OAuth Token")
                .instructionText("The OAuth2 authentication token")
                .build()
        )
        .build();
  }
}
