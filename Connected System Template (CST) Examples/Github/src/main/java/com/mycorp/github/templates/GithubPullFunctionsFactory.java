package com.mycorp.github.templates;

import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.textProperty;
import static com.appiancorp.connectedsystems.templateframework.sdk.configuration.DomainSpecificLanguage.type;

import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.TextPropertyDescriptor;


public class GithubPullFunctionsFactory {

  public static final String REPO = "repo";
  public static final String OWNER = "owner";
  public static final String USERNAME = "username";
  public static final String GITHUB_PULL_TYPE = "githubPullType";

  public static LocalTypeDescriptor getTypes(ConfigurationDescriptor configurationDescriptor) {
    TextPropertyDescriptor repo = textProperty().key(REPO)
        .label("Github Repository")
        .isExpressionable(true)
        .isRequired(true)
        .build();
    TextPropertyDescriptor owner = textProperty().key(OWNER)
        .label("Repository Owner")
        .instructionText("Who owns this repository")
        .isExpressionable(true)
        .isRequired(true)
        .build();
    TextPropertyDescriptor username = textProperty().key(USERNAME)
        .label("Username")
        .isExpressionable(true)
        .build();

    LocalTypeDescriptor.Builder propertyBuilder = type().name(GITHUB_PULL_TYPE)
        .properties(repo, owner, username);

    LocalTypeDescriptor localType = propertyBuilder.build();
    return localType;
  }
}
