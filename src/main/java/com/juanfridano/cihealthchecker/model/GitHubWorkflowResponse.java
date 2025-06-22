package com.juanfridano.cihealthchecker.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GitHubWorkflowResponse {
    @JsonProperty("total_count")
    private Long totalCount;
    @JsonProperty("workflow_runs")
    private List<WorkflowRun> workflowRuns;
}