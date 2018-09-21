package com.mycorp.github.templates;


public class GithubClientInfo {
  private final String authToken;

  public GithubClientInfo(String authToken) {
    this.authToken = authToken;
  }

  public String getAuthToken() {
    return authToken;
  }
}
