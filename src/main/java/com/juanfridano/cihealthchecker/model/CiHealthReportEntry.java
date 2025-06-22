package com.juanfridano.cihealthchecker.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CiHealthReportEntry {
    private String workflowName;
    private long totalRuns;
    private long failures;
    private double failureRate;
    private double avgDurationMinutes;
}
