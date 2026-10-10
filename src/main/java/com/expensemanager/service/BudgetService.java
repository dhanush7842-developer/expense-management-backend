package com.expensemanager.service;

import com.expensemanager.dto.BudgetRequest;
import com.expensemanager.dto.BudgetResponse;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.User;
import com.expensemanager.exception.BadRequestException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.BudgetRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.YearMonth;
import java.util.Locale;

@Service
public class BudgetService {

    // Alert levels sent to the frontend
    public static final String LEVEL_NONE = "NONE";
    public static final String LEVEL_NORMAL = "NORMAL";
    public static final String LEVEL_WARNING = "WARNING";
    public static final String LEVEL_CRITICAL = "CRITICAL";
    public static final String LEVEL_EXCEEDED = "EXCEEDED";

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // Create the budget for a month, or update it if it already exists
    @Transactional
    public BudgetResponse saveBudget(BudgetRequest request, String email) {
        User user = getUser(email);

        Budget budget = budgetRepository
                .findByUserIdAndMonthAndYear(user.getId(), request.getMonth(), request.getYear())
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setUser(user);
                    newBudget.setMonth(request.getMonth());
                    newBudget.setYear(request.getYear());
                    return newBudget;
                });

        budget.setAmount(request.getAmount());
        Budget savedBudget = budgetRepository.save(budget);

        return buildResponse(savedBudget, user.getId(), request.getMonth(), request.getYear());
    }

    // Budget of the current month
    @Transactional(readOnly = true)
    public BudgetResponse getCurrentBudget(String email) {
        YearMonth now = YearMonth.now();
        return getBudget(now.getMonthValue(), now.getYear(), email);
    }

    // Budget of any selected month
    @Transactional(readOnly = true)
    public BudgetResponse getBudget(Integer month, Integer year, String email) {
        if (month == null || year == null) {
            throw new BadRequestException("Both month and year are required");
        }
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Year must be between 2000 and 2100");
        }

        User user = getUser(email);
        Budget budget = budgetRepository
                .findByUserIdAndMonthAndYear(user.getId(), month, year)
                .orElse(null);

        return buildResponse(budget, user.getId(), month, year);
    }

    // ---------- helpers ----------

    // Spending is calculated from the real expenses in MySQL, never stored
    private BudgetResponse buildResponse(Budget budget, Long userId, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);

        BigDecimal totalSpent = expenseRepository.sumByUserIdAndDateRange(
                userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }

        // No budget set for this month
        if (budget == null) {
            return new BudgetResponse(null, month, year, BigDecimal.ZERO, totalSpent,
                    BigDecimal.ZERO, BigDecimal.ZERO, false, LEVEL_NONE, null);
        }

        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remaining = budgetAmount.subtract(totalSpent);

        BigDecimal percentageUsed = BigDecimal.ZERO;
        if (budgetAmount.signum() > 0) {
            percentageUsed = totalSpent
                    .multiply(HUNDRED)
                    .divide(budgetAmount, 2, RoundingMode.HALF_UP);
        }

        String alertLevel = determineAlertLevel(totalSpent, budgetAmount);
        String alertMessage = buildAlertMessage(alertLevel, totalSpent, budgetAmount);

        return new BudgetResponse(budget.getId(), month, year, budgetAmount,
                totalSpent, remaining, percentageUsed, true, alertLevel, alertMessage);
    }

    // Compares real amounts (not rounded percentages) so the boundaries are exact:
    // below 75% = NORMAL, 75% to below 90% = WARNING, 90% to below 100% = CRITICAL,
    // 100%+ = EXCEEDED
    private String determineAlertLevel(BigDecimal totalSpent, BigDecimal budgetAmount) {
        BigDecimal spentTimes100 = totalSpent.multiply(HUNDRED);

        if (totalSpent.compareTo(budgetAmount) >= 0) {
            return LEVEL_EXCEEDED;
        }
        if (spentTimes100.compareTo(budgetAmount.multiply(BigDecimal.valueOf(90))) >= 0) {
            return LEVEL_CRITICAL;
        }
        if (spentTimes100.compareTo(budgetAmount.multiply(BigDecimal.valueOf(75))) >= 0) {
            return LEVEL_WARNING;
        }
        return LEVEL_NORMAL;
    }

    private String buildAlertMessage(String alertLevel, BigDecimal totalSpent, BigDecimal budgetAmount) {
        // Whole percentage, rounded DOWN (so a WARNING never shows "90%")
        int wholePercent = totalSpent
                .multiply(HUNDRED)
                .divide(budgetAmount, 0, RoundingMode.DOWN)
                .intValue();

        switch (alertLevel) {
            case LEVEL_EXCEEDED:
                BigDecimal exceededBy = totalSpent.subtract(budgetAmount);
                if (exceededBy.signum() == 0) {
                    return "Budget limit reached: you've used 100% of your monthly budget.";
                }
                return "Budget exceeded by ₹" + formatAmount(exceededBy) + ".";
            case LEVEL_CRITICAL:
                return "Critical: You've used " + wholePercent + "% of your monthly budget.";
            case LEVEL_WARNING:
                return "Warning: You've used " + wholePercent + "% of your monthly budget.";
            default:
                return "You're using " + wholePercent + "% of your monthly budget.";
        }
    }

    // 500.00 -> "500", 1500.5 -> "1,500.50"
    private String formatAmount(BigDecimal amount) {
        String pattern = amount.stripTrailingZeros().scale() <= 0 ? "#,##0" : "#,##0.00";
        DecimalFormat formatter = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.US));
        return formatter.format(amount);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}