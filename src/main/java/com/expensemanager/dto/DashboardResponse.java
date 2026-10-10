package com.expensemanager.dto;

import java.util.List;

public class DashboardResponse {

    private DashboardSummaryResponse summary;
    private List<CategoryTotalResponse> categoryExpenses;
    private List<MonthlyTotalResponse> monthlyExpenses;

    public DashboardResponse() {
    }

    public DashboardResponse(DashboardSummaryResponse summary, List<CategoryTotalResponse> categoryExpenses,
            List<MonthlyTotalResponse> monthlyExpenses) {
        this.summary = summary;
        this.categoryExpenses = categoryExpenses;
        this.monthlyExpenses = monthlyExpenses;
    }

    public DashboardSummaryResponse getSummary() {
        return summary;
    }

    public void setSummary(DashboardSummaryResponse summary) {
        this.summary = summary;
    }

    public List<CategoryTotalResponse> getCategoryExpenses() {
        return categoryExpenses;
    }

    public void setCategoryExpenses(List<CategoryTotalResponse> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
    }

    public List<MonthlyTotalResponse> getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(List<MonthlyTotalResponse> monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }
}
