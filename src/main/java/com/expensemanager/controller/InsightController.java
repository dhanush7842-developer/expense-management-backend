package com.expensemanager.controller;

import com.expensemanager.dto.InsightResponse;
import com.expensemanager.service.InsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    // GET /api/insights
    @GetMapping
    public ResponseEntity<List<InsightResponse>> getInsights(Authentication authentication) {
        return ResponseEntity.ok(insightService.getInsights(authentication.getName()));
    }
}