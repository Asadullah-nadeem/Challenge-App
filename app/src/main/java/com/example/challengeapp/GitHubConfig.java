package com.example.challengeapp;

public enum GitHubConfig {
    PROFILE_URL("https://github.com/Asadullah-nadeem/"),
    API_URL("https://github-contributions-api.deno.dev/Asadullah-nadeem.json");

    private final String url;
    GitHubConfig(String url) { this.url = url; }
    public String getUrl() { return url; }
}
