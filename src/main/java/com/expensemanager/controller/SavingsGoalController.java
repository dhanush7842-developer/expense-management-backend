package com.expensemanager.controller;

import com.expensemanager.dto.AddMoneyRequest;
import com.expensemanager.dto.SavingsGoalRequest;
import com.expensemanager.dto.SavingsGoalResponse;
import com.expensemanager.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    // GET /api/savings-goals
    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getAllGoals(Authentication authentication) {
        return ResponseEntity.ok(savingsGoalService.getAllGoals(authentication.getName()));
    }

    // POST /api/savings-goals
    @PostMapping
    public ResponseEntity<SavingsGoalResponse> createGoal(@Valid @RequestBody SavingsGoalRequest request,
            Authentication authentication) {
        SavingsGoalResponse created = savingsGoalService.createGoal(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/savings-goals/{id}
    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponse> updateGoal(@PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(savingsGoalService.updateGoal(id, request, authentication.getName()));
    }

    // DELETE /api/savings-goals/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id, Authentication authentication) {
        savingsGoalService.deleteGoal(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // POST /api/savings-goals/{id}/add-money
    @PostMapping("/{id}/add-money")
    public ResponseEntity<SavingsGoalResponse> addMoney(@PathVariable Long id,
            @Valid @RequestBody AddMoneyRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(savingsGoalService.addMoney(id, request, authentication.getName()));
    }
}