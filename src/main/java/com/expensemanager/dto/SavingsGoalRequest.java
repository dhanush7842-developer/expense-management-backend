package com.expensemanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoalRequest {

    @NotBlank(message = "Goal name is required")
    @Size(max = 150, message = "Goal name must be at most 150 characters")
    private String goalName;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Target amount can have up to 8 digits and 2 decimals")
    private BigDecimal targetAmount;

    // Optional: empty on create = 0, empty on update = keep the current value
    @DecimalMin(value = "0.00", message = "Saved amount cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Saved amount can have up to 8 digits and 2 decimals")
    private BigDecimal savedAmount;

    @NotNull(message = "Target date is required")
    @FutureOrPresent(message = "Target date cannot be in the past")
    private LocalDate targetDate; // format from React: "2026-12-31"

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(BigDecimal savedAmount) {
        this.savedAmount = savedAmount;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
}