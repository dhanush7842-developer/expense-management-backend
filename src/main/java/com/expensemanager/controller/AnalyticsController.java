package com.expensemanager.controller;

import com.expensemanager.dto.AnalyticsSummaryResponse;
import com.expensemanager.dto.CategoryTotalResponse;
import com.expensemanager.dto.DailyTotalResponse;
import com.expensemanager.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // GET /api/analytics/summary?month=10&year=2026 (both optional, default =
    // current month)
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getSummary(month, year, authentication.getName()));
    }

    // GET /api/analytics/category-wise?month=10&year=2026
    @GetMapping("/category-wise")
    public ResponseEntity<List<CategoryTotalResponse>> getCategoryWise(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getCategoryWise(month, year, authentication.getName()));
    }

    // GET /api/analytics/daily?month=10&year=2026
    @GetMapping("/daily")
    public ResponseEntity<List<DailyTotalResponse>> getDailySpending(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getDailySpending(month, year, authentication.getName()));
    }
}
