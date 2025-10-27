package main.java.com.example.report;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;

/**
 * A Runnable task that simulates report generation in the background.
 * This task takes time to complete and may succeed or fail randomly.
 */
public class ReportTask implements Runnable {
    private final String reportId;
    private final ConcurrentMap<String, ReportResult> reportMap;
    private static final Random random = new Random();
    
    public ReportTask(String reportId, ConcurrentMap<String, ReportResult> reportMap) {
        this.reportId = reportId;
        this.reportMap = reportMap;
    }
    
    @Override
    public void run() {
        try {
            // Simulate report generation taking time (2-5 seconds)
            int processingTime = 2000 + random.nextInt(3000);
            Thread.sleep(processingTime);
            
            // Simulate occasional failures (20% chance)
            if (random.nextInt(100) < 20) {
                // Report generation failed
                reportMap.put(reportId, new ReportResult(
                    ReportStatus.FAILED, 
                    "Report generation failed due to data processing error"
                ));
            } else {
                // Report generation succeeded
                reportMap.put(reportId, new ReportResult(
                    ReportStatus.COMPLETED, 
                    "Report generated successfully with " + (1000 + random.nextInt(9000)) + " records"
                ));
            }
        } catch (InterruptedException e) {
            // Handle thread interruption
            reportMap.put(reportId, new ReportResult(
                ReportStatus.FAILED, 
                "Report generation was interrupted"
            ));
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Handle unexpected errors
            reportMap.put(reportId, new ReportResult(
                ReportStatus.FAILED, 
                "Unexpected error: " + e.getMessage()
            ));
        }
    }
}
