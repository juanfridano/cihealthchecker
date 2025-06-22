package com.juanfridano.cihealthchecker.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.juanfridano.cihealthchecker.model.GitHubWorkflowResponse;

@FeignClient(name = "githubClient", url = "https://api.github.com")
public interface GitHubClient {

    @GetMapping("/repos/{owner}/{repo}/actions/runs")
    GitHubWorkflowResponse getWorkflowRuns(
        @RequestHeader("Authorization") String token,
        @PathVariable("owner") String owner,
        @PathVariable("repo") String repo,
        @RequestParam("created") String created,
        @RequestParam("per_page") int perPage,
        @RequestParam("page") int page
    );
}