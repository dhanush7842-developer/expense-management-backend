package com.expensemanager.dto;

public class InsightResponse {

    private String type; // e.g. TOP_CATEGORY, AVERAGE_DAILY, BUDGET_USAGE
    private String severity; // INFO, SUCCESS, WARNING or DANGER (used by React for color and icon)
    private String message; // text shown to the user

    public InsightResponse(String type, String severity, String message) {
        this.type = type;
        this.severity = severity;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }
}