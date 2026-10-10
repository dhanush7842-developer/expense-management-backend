package com.expensemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyTotalResponse {

    private LocalDate date;
    private BigDecimal totalAmount;

    // Called directly by the JPQL query in ExpenseRepository
    public DailyTotalResponse(LocalDate date, BigDecimal totalAmount) {
        this.date = date;
        this.totalAmount = totalAmount;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    // Sent to React as "day" (1 to 31), used as the chart label
    public int getDay() {
        return date.getDayOfMonth();
    }
}
