package com.expensemanager.repository;

import com.expensemanager.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    // All goals of one user, nearest deadline first
    List<SavingsGoal> findByUserIdOrderByTargetDateAsc(Long userId);

    // Find a goal only if it belongs to this user
    Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);
}
