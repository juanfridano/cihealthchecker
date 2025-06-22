package com.juanfridano.cihealthchecker.model;

import lombok.Data;

import java.time.Duration;
import java.time.OffsetDateTime;

@Data
public class WorkflowRun {
    private String name;
    private String conclusion;
    private OffsetDateTime created_at;
    private OffsetDateTime run_started_at;
    private OffsetDateTime updated_at;

    public double getDurationMinutes() {
        if (run_started_at != null && updated_at != null) {
            return Duration.between(run_started_at, updated_at).toMinutes();
        }
        return 0;
    }
}
