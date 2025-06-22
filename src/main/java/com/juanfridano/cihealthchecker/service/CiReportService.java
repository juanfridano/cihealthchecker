package com.juanfridano.cihealthchecker.service;

import com.juanfridano.cihealthchecker.client.GitHubClient;
import com.juanfridano.cihealthchecker.config.GitHubProperties;
import com.juanfridano.cihealthchecker.exception.GitHubClientException;
import com.juanfridano.cihealthchecker.model.CiHealthReportEntry;
import com.juanfridano.cihealthchecker.model.GitHubWorkflowResponse;
import com.juanfridano.cihealthchecker.model.WorkflowRun;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CiReportService {

    private final GitHubClient gitHubClient;
    private final GitHubProperties gitHubProperties;

    public List<CiHealthReportEntry> generateReport(OffsetDateTime since) {
        String token = "Bearer " + gitHubProperties.getToken();
        String owner = gitHubProperties.getOwner();
        String repo = gitHubProperties.getRepo();
        String createdQuery = ">" + since.toLocalDate().toString(); // GitHub expects YYYY-MM-DD
    
        List<WorkflowRun> allRuns = new ArrayList<>();
        int page = 1;
    
        try {
            while (true) {
                GitHubWorkflowResponse response = gitHubClient.getWorkflowRuns(token, owner, repo, createdQuery, 100, page);
                List<WorkflowRun> pageRuns = response.getWorkflowRuns();
                if (pageRuns == null || pageRuns.isEmpty()) break;
                allRuns.addAll(pageRuns);
                page++;
            }
        } catch (FeignException e) {
            log.error("GitHub API call failed: {}", e.getMessage());
            throw new GitHubClientException("GitHub API error: " + e.status() + " - " + e.getMessage());
        }
    
        return allRuns.stream()
            .collect(Collectors.groupingBy(WorkflowRun::getName))
            .entrySet().stream()
            .map(entry -> {
                List<WorkflowRun> group = entry.getValue();
                long total = group.size();
                long failures = group.stream().filter(r -> !"success".equalsIgnoreCase(r.getConclusion())).count();
                double avg = group.stream().mapToDouble(WorkflowRun::getDurationMinutes).average().orElse(0);
                return CiHealthReportEntry.builder()
                    .workflowName(entry.getKey())
                    .totalRuns(total)
                    .failures(failures)
                    .failureRate((failures * 100.0 / total))
                    .avgDurationMinutes(avg)
                    .build();
            })
            .toList();
    }
}
