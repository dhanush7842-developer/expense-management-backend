package com.expensemanager.dto;

import java.math.BigDecimal;

public class ExpenseStats {

    private Long expenseCount;
    private BigDecimal totalAmount; // null when there are no expenses
    private BigDecimal highestAmount; // null when there are no expenses
    private BigDecimal lowestAmount; // null when there are no expenses

    // Called directly by the JPQL query in ExpenseRepository
    public ExpenseStats(Long expenseCount, BigDecimal totalAmount,
            BigDecimal highestAmount, BigDecimal lowestAmount) {
        this.expenseCount = expenseCount;
        this.totalAmount = totalAmount;
        this.highestAmount = highestAmount;
        this.lowestAmount = lowestAmount;
    }

    public Long getExpenseCount() {
        return expenseCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getHighestAmount() {
        return highestAmount;
    }

    public BigDecimal getLowestAmount() {
        return lowestAmount;
    }
}
