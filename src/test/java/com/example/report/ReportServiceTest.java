package test.java.com.example.report;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for the asynchronous report generation system.
 */
public class ReportServiceTest {
    
    private ExecutorService executorService;
    private ConcurrentHashMap<String, ReportResult> reportMap;
    private ReportService reportService;
    
    @Before
    public void setUp() {
        executorService = Executors.newFixedThreadPool(5);
        reportMap = new ConcurrentHashMap<>();
        reportService = new ReportService(executorService, reportMap);
    }
    
    @After
    public void tearDown() {
        reportService.shutdown();
    }
    
    /**
     * Test 1: Verify that requestReport returns immediately with a unique ID
     */
    @Test
    public void testRequestReportReturnsImmediately() {
        long startTime = System.currentTimeMillis();
        String reportId = reportService.requestReport();
        long endTime = System.currentTimeMillis();
        
        // Should return in less than 100ms (much faster than report generation time)
        assertTrue("Request should return immediately", (endTime - startTime) < 100);
        assertNotNull("Report ID should not be null", reportId);
        assertFalse("Report ID should not be empty", reportId.isEmpty());
    }
    
    /**
     * Test 2: Verify that each report request gets a unique identifier
     */
    @Test
    public void testUniqueIdentifiers() {
        Set<String> reportIds = new HashSet<>();
        
        // Request 10 reports
        for (int i = 0; i < 10; i++) {
            String reportId = reportService.requestReport();
            reportIds.add(reportId);
        }
        
        // All IDs should be unique
        assertEquals("All report IDs should be unique", 10, reportIds.size());
    }
    
    /**
     * Test 3: Verify initial status is PENDING
     */
    @Test
    public void testInitialStatusIsPending() {
        String reportId = reportService.requestReport();
        ReportResult result = reportService.getReportStatus(reportId);
        
        assertNotNull("Report result should not be null", result);
        assertEquals("Initial status should be PENDING", ReportStatus.PENDING, result.getStatus());
    }
    
    /**
     * Test 4: Verify that report eventually completes or fails
     */
    @Test(timeout = 10000)
    public void testReportEventuallyCompletesOrFails() throws InterruptedException {
        String reportId = reportService.requestReport();
        
        // Poll until status changes from PENDING
        ReportResult result = null;
        int maxAttempts = 50;
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            result = reportService.getReportStatus(reportId);
            if (result.getStatus() != ReportStatus.PENDING) {
                break;
            }
            Thread.sleep(200);
            attempts++;
        }
        
        assertNotNull("Report result should not be null", result);
        assertTrue("Report should complete or fail", 
            result.getStatus() == ReportStatus.COMPLETED || 
            result.getStatus() == ReportStatus.FAILED);
        assertNotNull("Detail should not be null", result.getDetail());
    }
    
    /**
     * Test 5: Verify status query for non-existent report
     */
    @Test
    public void testQueryNonExistentReport() {
        ReportResult result = reportService.getReportStatus("non-existent-id");
        assertNull("Non-existent report should return null", result);
    }
    
    /**
     * Test 6: Test multiple concurrent report requests
     */
    @Test(timeout = 50000)
    public void testMultipleConcurrentRequests() throws InterruptedException {
        int numReports = 20;
        List<String> reportIds = new ArrayList<>();
        
        // Submit multiple reports concurrently
        for (int i = 0; i < numReports; i++) {
            String reportId = reportService.requestReport();
            reportIds.add(reportId);
        }
        
        // Verify all IDs are unique
        Set<String> uniqueIds = new HashSet<>(reportIds);
        assertEquals("All report IDs should be unique", numReports, uniqueIds.size());
        
        // Wait for all reports to complete (20 reports with 3 threads, max 5 sec each = ~35 sec)
        Thread.sleep(40000);
        
        // Check all reports have final status
        int completed = 0;
        int failed = 0;
        
        for (String reportId : reportIds) {
            ReportResult result = reportService.getReportStatus(reportId);
            assertNotNull("Report result should not be null for ID: " + reportId, result);
            
            if (result.getStatus() == ReportStatus.COMPLETED) {
                completed++;
            } else if (result.getStatus() == ReportStatus.FAILED) {
                failed++;
            }
        }
        
        // All reports should have completed or failed
        assertEquals("All reports should have final status", numReports, completed + failed);
        assertTrue("At least some reports should complete", completed > 0);
    }
    
    /**
     * Test 7: Test thread safety with concurrent status queries
     */
    @Test(timeout = 15000)
    public void testConcurrentStatusQueries() throws InterruptedException {
        // Request several reports
        List<String> reportIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            reportIds.add(reportService.requestReport());
        }
        
        // Create multiple threads that query status concurrently
        CountDownLatch latch = new CountDownLatch(10);
        List<Exception> exceptions = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < 100; j++) {
                        for (String reportId : reportIds) {
                            reportService.getReportStatus(reportId);
                        }
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        // Wait for all threads to complete
        latch.await(10, TimeUnit.SECONDS);
        
        // Should not have any exceptions
        assertTrue("No exceptions should occur during concurrent queries", exceptions.isEmpty());
    }
    
    /**
     * Test 8: Test that completed reports have appropriate details
     */
    @Test(timeout = 10000)
    public void testCompletedReportDetails() throws InterruptedException {
        String reportId = reportService.requestReport();
        
        // Wait for completion
        ReportResult result = null;
        for (int i = 0; i < 50; i++) {
            result = reportService.getReportStatus(reportId);
            if (result.getStatus() != ReportStatus.PENDING) {
                break;
            }
            Thread.sleep(200);
        }
        
        assertNotNull("Report result should not be null", result);
        assertNotNull("Report detail should not be null", result.getDetail());
        assertFalse("Report detail should not be empty", result.getDetail().isEmpty());
        
        if (result.getStatus() == ReportStatus.COMPLETED) {
            assertTrue("Completed report should mention success or records", 
                result.getDetail().contains("success") || result.getDetail().contains("records"));
        } else if (result.getStatus() == ReportStatus.FAILED) {
            assertTrue("Failed report should mention error or failure", 
                result.getDetail().contains("error") || result.getDetail().contains("failed"));
        }
    }
    
    /**
     * Test 9: Test graceful shutdown
     */
    @Test(timeout = 15000)
    public void testGracefulShutdown() throws InterruptedException {
        // Submit several reports
        for (int i = 0; i < 5; i++) {
            reportService.requestReport();
        }
        
        // Shutdown and verify it completes
        reportService.shutdown();
        assertTrue("ExecutorService should be shutdown", executorService.isShutdown());
    }
    
    /**
     * Test 10: Test status progression (PENDING -> COMPLETED/FAILED)
     */
    @Test(timeout = 10000)
    public void testStatusProgression() throws InterruptedException {
        String reportId = reportService.requestReport();
        
        // Immediately check - should be PENDING
        ReportResult initialResult = reportService.getReportStatus(reportId);
        assertEquals("Initial status should be PENDING", ReportStatus.PENDING, initialResult.getStatus());
        
        // Wait and check again - should eventually change
        Thread.sleep(6000);
        ReportResult finalResult = reportService.getReportStatus(reportId);
        assertNotEquals("Status should change from PENDING", ReportStatus.PENDING, finalResult.getStatus());
    }
}
