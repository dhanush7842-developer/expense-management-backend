package com.expensemanager.dto;

import java.math.BigDecimal;

public class CategoryTotalResponse {

    private String categoryName;
    private BigDecimal totalAmount;

    // This constructor is called directly by the JPQL query in ExpenseRepository
    public CategoryTotalResponse(String categoryName, BigDecimal totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}