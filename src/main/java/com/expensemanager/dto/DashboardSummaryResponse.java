package com.expensemanager.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private BigDecimal totalExpenses;
    private BigDecimal currentMonthTotal;
    private long expenseCount;
    private String topCategory; // category with the highest spending, or "N/A"

    public DashboardSummaryResponse(BigDecimal totalExpenses, BigDecimal currentMonthTotal,
            long expenseCount, String topCategory) {
        this.totalExpenses = totalExpenses;
        this.currentMonthTotal = currentMonthTotal;
        this.expenseCount = expenseCount;
        this.topCategory = topCategory;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getCurrentMonthTotal() {
        return currentMonthTotal;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public String getTopCategory() {
        return topCategory;
    }
}