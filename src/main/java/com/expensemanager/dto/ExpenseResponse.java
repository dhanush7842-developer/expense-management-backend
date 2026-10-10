package com.expensemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseResponse {

    private Long id;
    private String title;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
    private Long categoryId;
    private String categoryName;

    public ExpenseResponse(Long id, String title, BigDecimal amount, LocalDate expenseDate,
            String description, Long categoryId, String categoryName) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
