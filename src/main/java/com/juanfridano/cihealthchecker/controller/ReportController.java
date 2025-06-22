package com.juanfridano.cihealthchecker.controller;

import java.util.List;

import com.juanfridano.cihealthchecker.model.CiHealthReportEntry;
import com.juanfridano.cihealthchecker.service.CiReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final CiReportService reportService;

    @GetMapping("/report")
    public List<CiHealthReportEntry> getReport(@RequestParam(defaultValue = "3") int daysBack) {
        OffsetDateTime since = OffsetDateTime.now().minusDays(daysBack);
        return reportService.generateReport(since);
    }
}