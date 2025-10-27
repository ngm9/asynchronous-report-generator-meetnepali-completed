package com.example.report;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class ReportService {
    private final ExecutorService executorService;
    private final ConcurrentMap<String, ReportResult> reportMap;

    public ReportService(ExecutorService executorService, ConcurrentMap<String, ReportResult> reportMap) {
        this.executorService = executorService;
        this.reportMap = reportMap;
    }

    /**
     * Request a new report generation. Returns immediately with a unique tracking ID.
     * The report is generated asynchronously in the background.
     * 
     * @return unique tracking ID for the report
     */
    public String requestReport() {
        // Generate a unique identifier for this report
        String reportId = UUID.randomUUID().toString();
        
        // Initialize the report with PENDING status
        reportMap.put(reportId, new ReportResult(ReportStatus.PENDING, "Report generation in progress"));
        
        // Submit the report generation task to the executor service (background thread)
        executorService.submit(new ReportTask(reportId, reportMap));
        
        // Return immediately without waiting
        return reportId;
    }

    /**
     * Get the current status of a report by its tracking ID.
     * 
     * @param reportId the unique tracking ID of the report
     * @return ReportResult containing status and details, or null if not found
     */
    public ReportResult getReportStatus(String reportId) {
        return reportMap.get(reportId);
    }

    /**
     * Shutdown the executor service gracefully.
     * Waits for running tasks to complete.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            // Wait up to 10 seconds for tasks to complete
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // Force shutdown if tasks don't complete in time
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
