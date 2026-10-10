package com.expensemanager.service;

import com.expensemanager.dto.ExpenseRequest;
import com.expensemanager.dto.ExpenseResponse;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses(String email) {
        User user = getUser(email);
        return expenseRepository.findByUserIdOrderByExpenseDateDescIdDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id, String email) {
        User user = getUser(email);
        Expense expense = findUserExpense(id, user.getId());
        return toResponse(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, String email) {
        User user = getUser(email);
        Category category = findUserCategory(request.getCategoryId(), user.getId());

        Expense expense = new Expense();
        expense.setTitle(request.getTitle().trim());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription());
        expense.setCategory(category);
        expense.setUser(user);

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request, String email) {
        User user = getUser(email);
        Expense expense = findUserExpense(id, user.getId());
        Category category = findUserCategory(request.getCategoryId(), user.getId());

        expense.setTitle(request.getTitle().trim());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription());
        expense.setCategory(category);

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(Long id, String email) {
        User user = getUser(email);
        Expense expense = findUserExpense(id, user.getId());
        expenseRepository.delete(expense);
    }

    // ---------- helpers ----------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Expense findUserExpense(Long expenseId, Long userId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + expenseId));
    }

    private Category findUserCategory(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getDescription(),
                expense.getCategory().getId(),
                expense.getCategory().getName());
    }
}
