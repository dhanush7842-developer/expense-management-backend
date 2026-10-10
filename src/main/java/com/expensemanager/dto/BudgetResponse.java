package com.expensemanager.dto;

import java.math.BigDecimal;

public class BudgetResponse {

    private Long id; // null when no budget is set
    private Integer month;
    private Integer year;
    private BigDecimal budgetAmount; // 0 when no budget is set
    private BigDecimal totalSpent; // real spending of that month, calculated from expenses
    private BigDecimal remainingAmount; // budgetAmount - totalSpent (negative if over budget)
    private BigDecimal percentageUsed; // totalSpent / budgetAmount * 100
    private boolean budgetSet;
    private String alertLevel; // NONE, NORMAL, WARNING, CRITICAL or EXCEEDED
    private String alertMessage; // text shown on the dashboard (null when NONE)

    public BudgetResponse(Long id, Integer month, Integer year, BigDecimal budgetAmount,
            BigDecimal totalSpent, BigDecimal remainingAmount,
            BigDecimal percentageUsed, boolean budgetSet,
            String alertLevel, String alertMessage) {
        this.id = id;
        this.month = month;
        this.year = year;
        this.budgetAmount = budgetAmount;
        this.totalSpent = totalSpent;
        this.remainingAmount = remainingAmount;
        this.percentageUsed = percentageUsed;
        this.budgetSet = budgetSet;
        this.alertLevel = alertLevel;
        this.alertMessage = alertMessage;
    }

    public Long getId() {
        return id;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getYear() {
        return year;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public BigDecimal getPercentageUsed() {
        return percentageUsed;
    }

    public boolean isBudgetSet() {
        return budgetSet;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public String getAlertMessage() {
        return alertMessage;
    }
}
