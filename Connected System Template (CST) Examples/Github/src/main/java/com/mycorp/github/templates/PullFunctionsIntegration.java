package com.mycorp.github.templates;

import static com.mycorp.github.templates.GithubPullFunctionsFactory.OWNER;
import static com.mycorp.github.templates.GithubPullFunctionsFactory.REPO;
import static com.mycorp.github.templates.GithubPullFunctionsFactory.USERNAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;

import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationError;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appiancorp.connectedsystems.templateframework.sdk.IntegrationTemplate;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.ConfigurationDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.LocalTypeDescriptor;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appiancorp.connectedsystems.templateframework.sdk.configuration.StateGenerator;
import com.google.common.collect.ImmutableMap;

@TemplateId(name = "pull")
public class PullFunctionsIntegration implements IntegrationTemplate {

  @Override
  public ConfigurationDescriptor getConfigurationDescriptor(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      PropertyPath updatedProperty,
      ExecutionContext executionContext) {
    LocalTypeDescriptor gitPullType = GithubPullFunctionsFactory.getTypes(integrationConfigDescriptor);

    StateGenerator stateGenerator = new StateGenerator(gitPullType);

    PropertyState propertyState;
    if (integrationConfigDescriptor == null) {
      propertyState = stateGenerator.generateDefaultState(gitPullType);
    } else {
      propertyState = stateGenerator.generateFromExistingState(gitPullType, integrationConfigDescriptor.getRootState());
    }
    return ConfigurationDescriptor.builder()
        .withState(propertyState)
        .withType(gitPullType)
        .build();
  }

  @Override
  public IntegrationResponse execute(
      ConfigurationDescriptor integrationConfigDescriptor,
      ConfigurationDescriptor connectedSystemConfigDescriptor,
      ExecutionContext executionContext) {

    GithubClientInfoFactory clientInfoFactory = new GithubClientInfoFactory();
    GithubClientInfo info = clientInfoFactory.getInfo(connectedSystemConfigDescriptor);

    GitHubClient client = new GitHubClient();
    client.setOAuth2Token(info.getAuthToken());

    PropertyState rootState = integrationConfigDescriptor.getRootState();
    String repo = (String)rootState.getValue(new PropertyPath(REPO));
    String owner = (String)rootState.getValue(new PropertyPath(OWNER));
    String username = (String)rootState.getValue(new PropertyPath(USERNAME));

    RepositoryService repoService = new RepositoryService(client);
    IssueService issueService = new IssueService(client);
    PullRequestService pullRequestService = new PullRequestService(client);

    try {

      Repository mainRepo = repoService.getRepository(owner,repo);

      String query = "author:"+username;
      List<SearchIssue> searchIssues = issueService.searchIssues(mainRepo,"open",query);
      ArrayList<PullRequest> pullRequests = new ArrayList<PullRequest>();

      for ( SearchIssue i : searchIssues) {
        pullRequests.add(pullRequestService.getPullRequest(mainRepo,i.getNumber()));
      }

      List<ImmutableMap<String,String>> shaMap = pullRequests.stream()
          .map(pr -> ImmutableMap.of("ref", pr.getHead().getRef(), "sha", pr.getHead().getSha()))
          .collect(Collectors.toList());
      return IntegrationResponse.forSuccess(ImmutableMap.of(
          "Result", 200,
          "Body", shaMap)).build();
    } catch (IOException e) {
      return IntegrationResponse.forError(
          IntegrationError.builder()
              .message(e.getMessage())
              .build())
          .build();
    }
  }

}
