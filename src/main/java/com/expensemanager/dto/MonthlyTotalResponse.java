package com.expensemanager.dto;

import java.math.BigDecimal;

public class MonthlyTotalResponse {

    private Integer year;
    private Integer month; // 1 = January ... 12 = December
    private BigDecimal totalAmount;

    // This constructor is called directly by the JPQL query in ExpenseRepository
    public MonthlyTotalResponse(Integer year, Integer month, BigDecimal totalAmount) {
        this.year = year;
        this.month = month;
        this.totalAmount = totalAmount;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
