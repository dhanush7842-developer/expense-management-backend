package com.expensemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SavingsGoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private BigDecimal remainingAmount; // targetAmount - savedAmount (calculated)
    private BigDecimal progressPercentage; // savedAmount / targetAmount * 100 (calculated)
    private LocalDate targetDate;
    private boolean completed;
    private LocalDateTime createdAt;

    public SavingsGoalResponse(Long id, String goalName, BigDecimal targetAmount,
            BigDecimal savedAmount, BigDecimal remainingAmount,
            BigDecimal progressPercentage, LocalDate targetDate,
            boolean completed, LocalDateTime createdAt) {
        this.id = id;
        this.goalName = goalName;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.remainingAmount = remainingAmount;
        this.progressPercentage = progressPercentage;
        this.targetDate = targetDate;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getGoalName() {
        return goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getSavedAmount() {
        return savedAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}