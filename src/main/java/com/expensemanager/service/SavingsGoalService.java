package com.expensemanager.service;

import com.expensemanager.dto.AddMoneyRequest;
import com.expensemanager.dto.SavingsGoalRequest;
import com.expensemanager.dto.SavingsGoalResponse;
import com.expensemanager.entity.SavingsGoal;
import com.expensemanager.entity.User;
import com.expensemanager.exception.BadRequestException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.SavingsGoalRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
            UserRepository userRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getAllGoals(String email) {
        User user = getUser(email);
        return savingsGoalRepository.findByUserIdOrderByTargetDateAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SavingsGoalResponse createGoal(SavingsGoalRequest request, String email) {
        User user = getUser(email);

        BigDecimal savedAmount = request.getSavedAmount() != null
                ? request.getSavedAmount()
                : BigDecimal.ZERO;
        validateSavedAmount(savedAmount, request.getTargetAmount());

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(request.getGoalName().trim());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setSavedAmount(savedAmount);
        goal.setTargetDate(request.getTargetDate());
        goal.setUser(user);

        return toResponse(savingsGoalRepository.save(goal));
    }

    @Transactional
    public SavingsGoalResponse updateGoal(Long id, SavingsGoalRequest request, String email) {
        User user = getUser(email);
        SavingsGoal goal = findUserGoal(id, user.getId());

        // If savedAmount is not sent, keep the current value
        BigDecimal savedAmount = request.getSavedAmount() != null
                ? request.getSavedAmount()
                : goal.getSavedAmount();
        validateSavedAmount(savedAmount, request.getTargetAmount());

        goal.setGoalName(request.getGoalName().trim());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setSavedAmount(savedAmount);
        goal.setTargetDate(request.getTargetDate());

        return toResponse(savingsGoalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long id, String email) {
        User user = getUser(email);
        SavingsGoal goal = findUserGoal(id, user.getId());
        savingsGoalRepository.delete(goal);
    }

    @Transactional
    public SavingsGoalResponse addMoney(Long id, AddMoneyRequest request, String email) {
        User user = getUser(email);
        SavingsGoal goal = findUserGoal(id, user.getId());

        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getSavedAmount());

        if (remaining.signum() <= 0) {
            throw new BadRequestException("This goal is already completed");
        }
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException(
                    "You can add at most ₹" + remaining.toPlainString() + " to reach this goal");
        }

        goal.setSavedAmount(goal.getSavedAmount().add(request.getAmount()));
        return toResponse(savingsGoalRepository.save(goal));
    }

    // ---------- helpers ----------

    private void validateSavedAmount(BigDecimal savedAmount, BigDecimal targetAmount) {
        if (savedAmount.compareTo(targetAmount) > 0) {
            throw new BadRequestException("Saved amount cannot be more than the target amount");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private SavingsGoal findUserGoal(Long goalId, Long userId) {
        return savingsGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id " + goalId));
    }

    // Remaining amount and progress are calculated here, never stored in the
    // database
    private SavingsGoalResponse toResponse(SavingsGoal goal) {
        BigDecimal target = goal.getTargetAmount();
        BigDecimal saved = goal.getSavedAmount();

        BigDecimal remaining = target.subtract(saved).max(BigDecimal.ZERO);

        BigDecimal progress = BigDecimal.ZERO;
        if (target.signum() > 0) {
            progress = saved.multiply(HUNDRED).divide(target, 2, RoundingMode.HALF_UP);
        }

        boolean completed = saved.compareTo(target) >= 0;

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getGoalName(),
                target,
                saved,
                remaining,
                progress,
                goal.getTargetDate(),
                completed,
                goal.getCreatedAt());
    }
}