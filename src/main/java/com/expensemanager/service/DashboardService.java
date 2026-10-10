package com.expensemanager.service;

import com.expensemanager.dto.CategoryTotalResponse;
import com.expensemanager.dto.DashboardResponse;
import com.expensemanager.dto.DashboardSummaryResponse;
import com.expensemanager.dto.MonthlyTotalResponse;
import com.expensemanager.entity.User;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public DashboardService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    // All dashboard data in one call
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String email, int year) {
        return new DashboardResponse(
                getSummary(email),
                getCategoryTotals(email),
                getMonthlyTotals(email, year)
        );
    }

    // Cards on the dashboard: totals, this month, count, top category
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(String email) {
        User user = getUser(email);
        Long userId = user.getId();

        BigDecimal total = expenseRepository.sumTotalByUserId(userId);
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        BigDecimal monthTotal = expenseRepository.sumByUserIdAndDateRange(userId, startDate, endDate);
        if (monthTotal == null) {
            monthTotal = BigDecimal.ZERO;
        }

        long count = expenseRepository.countByUserId(userId);

        // The query is sorted highest first, so the first item is the top category
        List<CategoryTotalResponse> categoryTotals = expenseRepository.findCategoryTotals(userId);
        String topCategory = categoryTotals.isEmpty() ? "N/A" : categoryTotals.get(0).getCategoryName();

        return new DashboardSummaryResponse(total, monthTotal, count, topCategory);
    }

    // Data for the pie chart
    @Transactional(readOnly = true)
    public List<CategoryTotalResponse> getCategoryTotals(String email) {
        User user = getUser(email);
        return expenseRepository.findCategoryTotals(user.getId());
    }

    // Data for the bar/line chart: always returns 12 months (missing months = 0)
    @Transactional(readOnly = true)
    public List<MonthlyTotalResponse> getMonthlyTotals(String email, int year) {
        User user = getUser(email);
        List<MonthlyTotalResponse> dbResults = expenseRepository.findMonthlyTotals(user.getId(), year);

        Map<Integer, BigDecimal> monthToAmount = new HashMap<>();
        for (MonthlyTotalResponse item : dbResults) {
            monthToAmount.put(item.getMonth(), item.getTotalAmount());
        }

        List<MonthlyTotalResponse> fullYear = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal amount = monthToAmount.getOrDefault(month, BigDecimal.ZERO);
            fullYear.add(new MonthlyTotalResponse(year, month, amount));
        }
        return fullYear;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
