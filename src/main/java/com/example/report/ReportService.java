package com.example.report;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentMap;

public class ReportService {
    private final ExecutorService executorService;
    private final ConcurrentMap<String, ReportResult> reportMap;

    public ReportService(ExecutorService executorService, ConcurrentMap<String, ReportResult> reportMap) {
        this.executorService = executorService;
        this.reportMap = reportMap;
    }

    public String requestReport() {
      
        return null;
    }

    public ReportResult getReportStatus(String reportId) {
       
        return null;
    }

    public void shutdown() {
       
    }
}
