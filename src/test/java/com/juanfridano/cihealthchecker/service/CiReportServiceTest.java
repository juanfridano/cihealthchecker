package com.juanfridano.cihealthchecker.service;

import com.juanfridano.cihealthchecker.client.GitHubClient;
import com.juanfridano.cihealthchecker.config.GitHubProperties;
import com.juanfridano.cihealthchecker.exception.GitHubClientException;
import com.juanfridano.cihealthchecker.model.CiHealthReportEntry;
import com.juanfridano.cihealthchecker.model.GitHubWorkflowResponse;
import com.juanfridano.cihealthchecker.model.WorkflowRun;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CiReportServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private GitHubProperties gitHubProperties;

    private CiReportService ciReportService;

    @BeforeEach
    void setUp() {
        ciReportService = new CiReportService(gitHubClient, gitHubProperties);
        
        // Setup common properties
        when(gitHubProperties.getToken()).thenReturn("test-token");
        when(gitHubProperties.getOwner()).thenReturn("test-owner");
        when(gitHubProperties.getRepo()).thenReturn("test-repo");
    }

    @Test
    void generateReport_WithSuccessfulWorkflowRuns_ShouldReturnCorrectReport() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        WorkflowRun successfulRun1 = createWorkflowRun("Test Workflow", "success", 10.0);
        WorkflowRun successfulRun2 = createWorkflowRun("Test Workflow", "success", 15.0);
        WorkflowRun failedRun = createWorkflowRun("Test Workflow", "failure", 20.0);
        
        GitHubWorkflowResponse response = new GitHubWorkflowResponse();
        response.setWorkflowRuns(Arrays.asList(successfulRun1, successfulRun2, failedRun));
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenReturn(response);
        
        // When
        List<CiHealthReportEntry> result = ciReportService.generateReport(since);
        
        // Then
        assertThat(result).hasSize(1);
        CiHealthReportEntry entry = result.get(0);
        assertThat(entry.getWorkflowName()).isEqualTo("Test Workflow");
        assertThat(entry.getTotalRuns()).isEqualTo(3);
        assertThat(entry.getFailures()).isEqualTo(1);
        assertThat(entry.getFailureRate()).isEqualTo(33.33333333333333);
        assertThat(entry.getAvgDurationMinutes()).isEqualTo(15.0);
    }

    @Test
    void generateReport_WithMultipleWorkflows_ShouldGroupByWorkflowName() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        WorkflowRun workflow1Run1 = createWorkflowRun("Workflow 1", "success", 10.0);
        WorkflowRun workflow1Run2 = createWorkflowRun("Workflow 1", "failure", 15.0);
        WorkflowRun workflow2Run1 = createWorkflowRun("Workflow 2", "success", 20.0);
        WorkflowRun workflow2Run2 = createWorkflowRun("Workflow 2", "success", 25.0);
        
        GitHubWorkflowResponse response = new GitHubWorkflowResponse();
        response.setWorkflowRuns(Arrays.asList(workflow1Run1, workflow1Run2, workflow2Run1, workflow2Run2));
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenReturn(response);
        
        // When
        List<CiHealthReportEntry> result = ciReportService.generateReport(since);
        
        // Then
        assertThat(result).hasSize(2);
        
        CiHealthReportEntry workflow1Entry = result.stream()
            .filter(entry -> "Workflow 1".equals(entry.getWorkflowName()))
            .findFirst()
            .orElseThrow();
        assertThat(workflow1Entry.getTotalRuns()).isEqualTo(2);
        assertThat(workflow1Entry.getFailures()).isEqualTo(1);
        assertThat(workflow1Entry.getFailureRate()).isEqualTo(50.0);
        assertThat(workflow1Entry.getAvgDurationMinutes()).isEqualTo(12.5);
        
        CiHealthReportEntry workflow2Entry = result.stream()
            .filter(entry -> "Workflow 2".equals(entry.getWorkflowName()))
            .findFirst()
            .orElseThrow();
        assertThat(workflow2Entry.getTotalRuns()).isEqualTo(2);
        assertThat(workflow2Entry.getFailures()).isEqualTo(0);
        assertThat(workflow2Entry.getFailureRate()).isEqualTo(0.0);
        assertThat(workflow2Entry.getAvgDurationMinutes()).isEqualTo(22.5);
    }

    @Test
    void generateReport_WithPagination_ShouldHandleMultiplePages() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        WorkflowRun page1Run1 = createWorkflowRun("Test Workflow", "success", 10.0);
        WorkflowRun page1Run2 = createWorkflowRun("Test Workflow", "success", 15.0);
        
        WorkflowRun page2Run1 = createWorkflowRun("Test Workflow", "failure", 20.0);
        WorkflowRun page2Run2 = createWorkflowRun("Test Workflow", "success", 25.0);
        
        GitHubWorkflowResponse page1Response = new GitHubWorkflowResponse();
        page1Response.setWorkflowRuns(Arrays.asList(page1Run1, page1Run2));
        
        GitHubWorkflowResponse page2Response = new GitHubWorkflowResponse();
        page2Response.setWorkflowRuns(Arrays.asList(page2Run1, page2Run2));
        
        GitHubWorkflowResponse emptyResponse = new GitHubWorkflowResponse();
        emptyResponse.setWorkflowRuns(Collections.emptyList());
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenReturn(page1Response);
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(2)
        )).thenReturn(page2Response);
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(3)
        )).thenReturn(emptyResponse);
        
        // When
        List<CiHealthReportEntry> result = ciReportService.generateReport(since);
        
        // Then
        assertThat(result).hasSize(1);
        CiHealthReportEntry entry = result.get(0);
        assertThat(entry.getWorkflowName()).isEqualTo("Test Workflow");
        assertThat(entry.getTotalRuns()).isEqualTo(4);
        assertThat(entry.getFailures()).isEqualTo(1);
        assertThat(entry.getFailureRate()).isEqualTo(25.0);
        assertThat(entry.getAvgDurationMinutes()).isEqualTo(17.5);
    }

    @Test
    void generateReport_WithEmptyResponse_ShouldReturnEmptyList() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        GitHubWorkflowResponse emptyResponse = new GitHubWorkflowResponse();
        emptyResponse.setWorkflowRuns(Collections.emptyList());
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenReturn(emptyResponse);
        
        // When
        List<CiHealthReportEntry> result = ciReportService.generateReport(since);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void generateReport_WithNullWorkflowRuns_ShouldReturnEmptyList() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        GitHubWorkflowResponse nullResponse = new GitHubWorkflowResponse();
        nullResponse.setWorkflowRuns(null);
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenReturn(nullResponse);
        
        // When
        List<CiHealthReportEntry> result = ciReportService.generateReport(since);
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void generateReport_WithGitHubApiError_ShouldThrowGitHubClientException() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        FeignException feignException = new FeignException.InternalServerError(
            "GitHub API error",
            null,
            "Internal Server Error".getBytes()
        );
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenThrow(feignException);
        
        // When & Then
        assertThatThrownBy(() -> ciReportService.generateReport(since))
            .isInstanceOf(GitHubClientException.class)
            .hasMessageContaining("GitHub API error: 500");
    }

    @Test
    void generateReport_WithDifferentConclusionValues_ShouldCountFailuresCorrectly() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        WorkflowRun successRun = createWorkflowRun("Test Workflow", "success", 10.0);
        WorkflowRun failureRun = createWorkflowRun("Test Workflow", "failure", 15.0);
        WorkflowRun cancelledRun = createWorkflowRun("Test Workflow", "cancelled", 20.0);
        WorkflowRun timedOutRun = createWorkflowRun("Test Workflow", "timed_out", 25.0);
        WorkflowRun nullConclusionRun = createWorkflowRun("Test Workflow", null, 30.0);
        
        GitHubWorkflowResponse response = new GitHubWorkflowResponse();
        response.setWorkflowRuns(Arrays.asList(successRun, failureRun, cancelledRun, timedOutRun, nullConclusionRun));
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenReturn(response);
        
        // When
        List<CiHealthReportEntry> result = ciReportService.generateReport(since);
        
        // Then
        assertThat(result).hasSize(1);
        CiHealthReportEntry entry = result.get(0);
        assertThat(entry.getTotalRuns()).isEqualTo(5);
        assertThat(entry.getFailures()).isEqualTo(4); // failure, cancelled, timed_out, null
        assertThat(entry.getFailureRate()).isEqualTo(80.0);
    }

    @Test
    void generateReport_WithZeroDurationRuns_ShouldHandleAverageCalculation() {
        // Given
        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        String expectedCreatedQuery = ">" + since.toLocalDate().toString();
        
        WorkflowRun zeroDurationRun = createWorkflowRun("Test Workflow", "success", 0.0);
        WorkflowRun normalDurationRun = createWorkflowRun("Test Workflow", "success", 10.0);
        
        GitHubWorkflowResponse response = new GitHubWorkflowResponse();
        response.setWorkflowRuns(Arrays.asList(zeroDurationRun, normalDurationRun));
        
        when(gitHubClient.getWorkflowRuns(
            eq("Bearer test-token"),
            eq("test-owner"),
            eq("test-repo"),
            eq(expectedCreatedQuery),
            eq(100),
            eq(1)
        )).thenReturn(response);
        
        // When
        List<CiHealthReportEntry> result = ciReportService.generateReport(since);
        
        // Then
        assertThat(result).hasSize(1);
        CiHealthReportEntry entry = result.get(0);
        assertThat(entry.getAvgDurationMinutes()).isEqualTo(5.0);
    }

    private WorkflowRun createWorkflowRun(String name, String conclusion, double durationMinutes) {
        WorkflowRun run = new WorkflowRun();
        run.setName(name);
        run.setConclusion(conclusion);
        
        // Set timestamps to create the desired duration
        OffsetDateTime startTime = OffsetDateTime.now().minusMinutes((long) durationMinutes);
        OffsetDateTime endTime = OffsetDateTime.now();
        run.setRun_started_at(startTime);
        run.setUpdated_at(endTime);
        
        return run;
    }
} 