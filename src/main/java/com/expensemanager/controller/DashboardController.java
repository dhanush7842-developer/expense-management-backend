package com.expensemanager.controller;

import com.expensemanager.dto.CategoryTotalResponse;
import com.expensemanager.dto.DashboardResponse;
import com.expensemanager.dto.DashboardSummaryResponse;
import com.expensemanager.dto.MonthlyTotalResponse;
import com.expensemanager.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // GET /api/dashboard?year=2026 (combined summary, category-wise, monthly)
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) Integer year,
            Authentication authentication) {
        int selectedYear = (year != null) ? year : Year.now().getValue();
        return ResponseEntity.ok(dashboardService.getDashboard(authentication.getName(), selectedYear));
    }

    // GET /api/dashboard/summary
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getSummary(authentication.getName()));
    }

    // GET /api/dashboard/category-wise
    @GetMapping("/category-wise")
    public ResponseEntity<List<CategoryTotalResponse>> getCategoryWise(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getCategoryTotals(authentication.getName()));
    }

    // GET /api/dashboard/monthly?year=2026 (year is optional)
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyTotalResponse>> getMonthly(
            @RequestParam(required = false) Integer year,
            Authentication authentication) {

        int selectedYear = (year != null) ? year : Year.now().getValue();
        return ResponseEntity.ok(dashboardService.getMonthlyTotals(authentication.getName(), selectedYear));
    }
}