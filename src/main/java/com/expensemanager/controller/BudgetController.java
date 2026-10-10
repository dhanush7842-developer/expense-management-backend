package com.expensemanager.controller;

import com.expensemanager.dto.BudgetRequest;
import com.expensemanager.dto.BudgetResponse;
import com.expensemanager.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // PUT /api/budgets (create or update)
    @PutMapping
    public ResponseEntity<BudgetResponse> saveBudget(@Valid @RequestBody BudgetRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(budgetService.saveBudget(request, authentication.getName()));
    }

    // GET /api/budgets/current
    @GetMapping("/current")
    public ResponseEntity<BudgetResponse> getCurrentBudget(Authentication authentication) {
        return ResponseEntity.ok(budgetService.getCurrentBudget(authentication.getName()));
    }

    // GET /api/budgets?month=10&year=2026
    @GetMapping
    public ResponseEntity<BudgetResponse> getBudget(@RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Authentication authentication) {
        return ResponseEntity.ok(budgetService.getBudget(month, year, authentication.getName()));
    }
}
