package com.mycorp.github.templates;

import static com.mycorp.github.templates.GithubConnectedSystemTypeFactory.AUTH_TOKEN;

import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;

public class GithubClientInfoFactory {

  public GithubClientInfo getInfo(ConfigurationDescriptor configurationDescriptor) {
    PropertyState rootState = configurationDescriptor.getRootState();
    String authToken = (String)rootState.getValue(new PropertyPath(AUTH_TOKEN));
    return new GithubClientInfo(authToken);
  }
}
