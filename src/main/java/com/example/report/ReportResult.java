package com.example.report;

public class ReportResult {
    private final ReportStatus status;
    private final String detail;

    public ReportResult(ReportStatus status, String detail) {
        this.status = status;
        this.detail = detail;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
