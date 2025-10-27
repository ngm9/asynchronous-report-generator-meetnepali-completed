package com.example.report;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        ConcurrentHashMap<String, ReportResult> reportMap = new ConcurrentHashMap<>();
        ReportService reportService = new ReportService(executorService, reportMap);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("1. Request report\n2. Check status\n3. Exit");
            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    String id = reportService.requestReport();
                    System.out.println("Your report tracking ID: " + id);
                    break;
                case "2":
                    System.out.print("Enter tracking ID: ");
                    String reportId = scanner.nextLine();
                    ReportResult result = reportService.getReportStatus(reportId);
                    if (result == null) {
                        System.out.println("Report ID not found.");
                    } else {
                        System.out.println("Status: " + result.getStatus());
                        if (result.getStatus() != ReportStatus.PENDING) {
                            System.out.println("Detail: " + result.getDetail());
                        }
                    }
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid input.");
            }
        }
        reportService.shutdown();
        System.out.println("Application exited.");
    }
}
