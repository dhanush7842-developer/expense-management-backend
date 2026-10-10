package com.expensemanager.service;

import com.expensemanager.dto.AnalyticsSummaryResponse;
import com.expensemanager.dto.CategoryTotalResponse;
import com.expensemanager.dto.DailyTotalResponse;
import com.expensemanager.dto.ExpenseStats;
import com.expensemanager.entity.User;
import com.expensemanager.exception.BadRequestException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public AnalyticsService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // Total, count, average, highest and lowest expense of the selected month
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(Integer month, Integer year, String email) {
        User user = getUser(email);
        YearMonth yearMonth = resolveMonth(month, year);

        ExpenseStats stats = expenseRepository.findStatsByUserIdAndDateRange(
                user.getId(), yearMonth.atDay(1), yearMonth.atEndOfMonth());

        long count = stats.getExpenseCount() == null ? 0 : stats.getExpenseCount();
        BigDecimal total = orZero(stats.getTotalAmount());

        // Average = total / count, rounded to 2 decimals (0 when there are no expenses)
        BigDecimal average = BigDecimal.ZERO;
        if (count > 0) {
            average = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }

        return new AnalyticsSummaryResponse(
                yearMonth.getMonthValue(),
                yearMonth.getYear(),
                total,
                count,
                average,
                orZero(stats.getHighestAmount()),
                orZero(stats.getLowestAmount()));
    }

    // Category-wise spending of the selected month, highest first
    @Transactional(readOnly = true)
    public List<CategoryTotalResponse> getCategoryWise(Integer month, Integer year, String email) {
        User user = getUser(email);
        YearMonth yearMonth = resolveMonth(month, year);

        return expenseRepository.findCategoryTotalsByDateRange(
                user.getId(), yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    // Daily spending: always returns every day of the month (days without expenses
    // = 0)
    @Transactional(readOnly = true)
    public List<DailyTotalResponse> getDailySpending(Integer month, Integer year, String email) {
        User user = getUser(email);
        YearMonth yearMonth = resolveMonth(month, year);

        List<DailyTotalResponse> dbResults = expenseRepository.findDailyTotals(
                user.getId(), yearMonth.atDay(1), yearMonth.atEndOfMonth());

        Map<LocalDate, BigDecimal> dateToAmount = new HashMap<>();
        for (DailyTotalResponse item : dbResults) {
            dateToAmount.put(item.getDate(), item.getTotalAmount());
        }

        List<DailyTotalResponse> fullMonth = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            fullMonth.add(new DailyTotalResponse(date, dateToAmount.getOrDefault(date, BigDecimal.ZERO)));
        }
        return fullMonth;
    }

    // ---------- helpers ----------

    // Both missing = current month. Only one of them = error.
    private YearMonth resolveMonth(Integer month, Integer year) {
        if (month == null && year == null) {
            return YearMonth.now();
        }
        if (month == null || year == null) {
            throw new BadRequestException("Both month and year are required");
        }
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Year must be between 2000 and 2100");
        }
        return YearMonth.of(year, month);
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
