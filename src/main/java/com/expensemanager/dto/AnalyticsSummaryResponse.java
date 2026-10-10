package com.expensemanager.dto;

import java.math.BigDecimal;

public class AnalyticsSummaryResponse {

    private Integer month;
    private Integer year;
    private BigDecimal totalAmount;
    private long expenseCount;
    private BigDecimal averageExpense;
    private BigDecimal highestExpense;
    private BigDecimal lowestExpense;

    public AnalyticsSummaryResponse(Integer month, Integer year, BigDecimal totalAmount,
            long expenseCount, BigDecimal averageExpense,
            BigDecimal highestExpense, BigDecimal lowestExpense) {
        this.month = month;
        this.year = year;
        this.totalAmount = totalAmount;
        this.expenseCount = expenseCount;
        this.averageExpense = averageExpense;
        this.highestExpense = highestExpense;
        this.lowestExpense = lowestExpense;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getYear() {
        return year;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public BigDecimal getAverageExpense() {
        return averageExpense;
    }

    public BigDecimal getHighestExpense() {
        return highestExpense;
    }

    public BigDecimal getLowestExpense() {
        return lowestExpense;
    }
}