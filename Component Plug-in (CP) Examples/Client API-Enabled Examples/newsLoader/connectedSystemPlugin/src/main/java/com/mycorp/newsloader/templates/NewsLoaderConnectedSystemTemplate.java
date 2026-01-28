package com.mycorp.newsloader.templates;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;

/**
 * This Connected System template serves as a secure vault for mock credentials
 * associated with a hypothetical third-party News API.
 *
 * NOTE: This configuration is for demonstration purposes only. It does not
 * influence active Client API transactions, but rather illustrates best
 * practices for centralized credential management within Connected Systems.
 */

@TemplateId(name = "NewsLoaderConnectedSystemTemplate")
public class NewsLoaderConnectedSystemTemplate extends SimpleTestableConnectedSystemTemplate {
  public static final String MOCK_NEWS_CORP_PASSWORD= "mockNewsCorpPassword";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {
    return simpleConfiguration.setProperties(
        textProperty(MOCK_NEWS_CORP_PASSWORD)
            .label("Password for Mock_News_Corp authentication")
            .instructionText("Just a mock simulation of 3rd party API call.")
            .isRequired(true)
            .isImportCustomizable(true)
            .build()
    );
  }

  @Override
  protected TestConnectionResult testConnection(SimpleConfiguration configuration, ExecutionContext executionContext) {
    return TestConnectionResult.success();
  }
}
