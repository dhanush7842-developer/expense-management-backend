package com.expensemanager.service;

import com.expensemanager.dto.BudgetResponse;
import com.expensemanager.dto.CategoryTotalResponse;
import com.expensemanager.dto.InsightResponse;
import com.expensemanager.entity.User;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class InsightService {

    private static final String SEVERITY_INFO = "INFO";
    private static final String SEVERITY_SUCCESS = "SUCCESS";
    private static final String SEVERITY_WARNING = "WARNING";
    private static final String SEVERITY_DANGER = "DANGER";

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // A category change smaller than this (in %) is not worth showing
    private static final int MIN_CHANGE_PERCENT = 10;

    // Show at most this many "increased / decreased" insights
    private static final int MAX_CATEGORY_CHANGES = 3;

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetService budgetService;

    public InsightService(ExpenseRepository expenseRepository,
            UserRepository userRepository,
            BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.budgetService = budgetService;
    }

    @Transactional(readOnly = true)
    public List<InsightResponse> getInsights(String email) {
        User user = getUser(email);
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);

        // Category totals from the 1st of this month until today (highest first)
        List<CategoryTotalResponse> categories = expenseRepository.findCategoryTotalsByDateRange(
                user.getId(), currentMonth.atDay(1), today);

        // Same budget numbers that the budget card uses
        BudgetResponse budget = budgetService.getCurrentBudget(email);

        List<InsightResponse> insights = new ArrayList<>();

        if (categories.isEmpty()) {
            insights.add(new InsightResponse("NO_EXPENSES", SEVERITY_INFO,
                    "No expenses recorded this month yet. Add expenses to see spending insights."));
            addBudgetInsights(insights, budget);
            return insights;
        }

        addBudgetInsights(insights, budget);
        addTopCategoryInsight(insights, categories);
        addAverageDailyInsight(insights, categories, today);
        addCategoryChangeInsights(insights, categories, user.getId(), currentMonth, today);

        return insights;
    }

    // ---------- Insight rules ----------

    private void addBudgetInsights(List<InsightResponse> insights, BudgetResponse budget) {
        if (!budget.isBudgetSet()) {
            insights.add(new InsightResponse("NO_BUDGET", SEVERITY_INFO,
                    "You haven't set a monthly budget yet. Set one on the dashboard to track your spending."));
            return;
        }

        BigDecimal spent = budget.getTotalSpent();
        BigDecimal budgetAmount = budget.getBudgetAmount();
        String alertLevel = budget.getAlertLevel();

        // Budget exceeded: show how much, and skip the "remaining" insight
        if (BudgetService.LEVEL_EXCEEDED.equals(alertLevel)) {
            BigDecimal exceededBy = spent.subtract(budgetAmount);
            String message = exceededBy.signum() == 0
                    ? "You have reached 100% of your monthly budget."
                    : "You have exceeded your monthly budget by \u20B9" + formatAmount(exceededBy) + ".";
            insights.add(new InsightResponse("BUDGET_USAGE", SEVERITY_DANGER, message));
            return;
        }

        // Whole percentage, rounded DOWN (same rule as the budget alert)
        int wholePercent = spent
                .multiply(HUNDRED)
                .divide(budgetAmount, 0, RoundingMode.DOWN)
                .intValue();

        if (BudgetService.LEVEL_CRITICAL.equals(alertLevel)) {
            insights.add(new InsightResponse("BUDGET_USAGE", SEVERITY_DANGER,
                    "You have already used " + wholePercent + "% of your monthly budget."));
        } else if (BudgetService.LEVEL_WARNING.equals(alertLevel)) {
            insights.add(new InsightResponse("BUDGET_USAGE", SEVERITY_WARNING,
                    "You have already used " + wholePercent + "% of your monthly budget."));
        } else {
            insights.add(new InsightResponse("BUDGET_USAGE", SEVERITY_INFO,
                    "You have used " + wholePercent + "% of your monthly budget."));
        }

        insights.add(new InsightResponse("BUDGET_REMAINING", SEVERITY_INFO,
                "You have \u20B9" + formatAmount(budget.getRemainingAmount()) + " remaining in your monthly budget."));
    }

    // The list is already sorted highest first, so the first item is the top
    // category
    private void addTopCategoryInsight(List<InsightResponse> insights,
            List<CategoryTotalResponse> categories) {
        CategoryTotalResponse top = categories.get(0);
        insights.add(new InsightResponse("TOP_CATEGORY", SEVERITY_INFO,
                top.getCategoryName() + " is your highest spending category this month (\u20B9"
                        + formatAmount(top.getTotalAmount()) + ")."));
    }

    // Average daily spending = spent so far / number of days passed in this month
    private void addAverageDailyInsight(List<InsightResponse> insights,
            List<CategoryTotalResponse> categories,
            LocalDate today) {
        BigDecimal totalSoFar = BigDecimal.ZERO;
        for (CategoryTotalResponse category : categories) {
            totalSoFar = totalSoFar.add(category.getTotalAmount());
        }

        BigDecimal average = totalSoFar.divide(
                BigDecimal.valueOf(today.getDayOfMonth()), 2, RoundingMode.HALF_UP);

        insights.add(new InsightResponse("AVERAGE_DAILY", SEVERITY_INFO,
                "Your average daily spending this month is \u20B9" + formatAmount(average) + "."));
    }

    // Compare each category with the SAME days of last month (a fair comparison)
    private void addCategoryChangeInsights(List<InsightResponse> insights,
            List<CategoryTotalResponse> currentCategories,
            Long userId,
            YearMonth currentMonth,
            LocalDate today) {
        YearMonth lastMonth = currentMonth.minusMonths(1);
        int lastEndDay = Math.min(today.getDayOfMonth(), lastMonth.lengthOfMonth());

        List<CategoryTotalResponse> previousCategories = expenseRepository.findCategoryTotalsByDateRange(
                userId, lastMonth.atDay(1), lastMonth.atDay(lastEndDay));

        Map<String, BigDecimal> previousTotals = new HashMap<>();
        for (CategoryTotalResponse previous : previousCategories) {
            previousTotals.put(previous.getCategoryName(), previous.getTotalAmount());
        }

        List<CategoryChange> changes = new ArrayList<>();
        for (CategoryTotalResponse current : currentCategories) {
            BigDecimal previous = previousTotals.get(current.getCategoryName());

            // Cannot calculate a percentage without previous spending
            if (previous == null || previous.signum() <= 0) {
                continue;
            }

            int percent = current.getTotalAmount()
                    .subtract(previous)
                    .multiply(HUNDRED)
                    .divide(previous, 0, RoundingMode.HALF_UP)
                    .intValue();

            if (Math.abs(percent) >= MIN_CHANGE_PERCENT) {
                changes.add(new CategoryChange(current.getCategoryName(), percent));
            }
        }

        // Biggest changes first, and keep only the top few
        changes.sort((a, b) -> Integer.compare(Math.abs(b.percent), Math.abs(a.percent)));
        int limit = Math.min(changes.size(), MAX_CATEGORY_CHANGES);

        for (int i = 0; i < limit; i++) {
            CategoryChange change = changes.get(i);
            if (change.percent > 0) {
                insights.add(new InsightResponse("CATEGORY_INCREASE", SEVERITY_WARNING,
                        "You spent " + change.percent + "% more on " + change.categoryName
                                + " compared with the same period last month."));
            } else {
                insights.add(new InsightResponse("CATEGORY_DECREASE", SEVERITY_SUCCESS,
                        "Your " + change.categoryName + " spending decreased by "
                                + Math.abs(change.percent) + "% compared with the same period last month."));
            }
        }
    }

    // ---------- helpers ----------

    // 450.00 -> "450", 1500.5 -> "1,500.50"
    private String formatAmount(BigDecimal amount) {
        String pattern = amount.stripTrailingZeros().scale() <= 0 ? "#,##0" : "#,##0.00";
        DecimalFormat formatter = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(Locale.US));
        return formatter.format(amount);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Small holder class: positive percent = increase, negative percent = decrease
    private static class CategoryChange {
        private final String categoryName;
        private final int percent;

        CategoryChange(String categoryName, int percent) {
            this.categoryName = categoryName;
            this.percent = percent;
        }
    }
}
