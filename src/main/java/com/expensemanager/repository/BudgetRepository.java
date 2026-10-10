package com.expensemanager.repository;

import com.expensemanager.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Finds the budget of one user for one month (the user comes from the JWT,
    // never from the frontend)
    Optional<Budget> findByUserIdAndMonthAndYear(Long userId, Integer month, Integer year);
}
