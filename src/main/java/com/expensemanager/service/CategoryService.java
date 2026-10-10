package com.expensemanager.service;

import com.expensemanager.dto.CategoryRequest;
import com.expensemanager.dto.CategoryResponse;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.exception.BadRequestException;
import com.expensemanager.exception.DuplicateResourceException;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(String email) {
        User user = getUser(email);
        return categoryRepository.findByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, String email) {
        User user = getUser(email);
        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndUserId(name, user.getId())) {
            throw new DuplicateResourceException("Category '" + name + "' already exists");
        }

        Category category = new Category(name, request.getDescription(), user);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request, String email) {
        User user = getUser(email);
        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(name, user.getId(), id)) {
            throw new DuplicateResourceException("Category '" + name + "' already exists");
        }

        category.setName(name);
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id, String email) {
        User user = getUser(email);
        Category category = categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        // Rule: a category that has expenses cannot be deleted
        if (expenseRepository.existsByCategoryId(id)) {
            throw new BadRequestException(
                    "Cannot delete this category because it has expenses. Delete or move those expenses first.");
        }

        categoryRepository.delete(category);
    }

    // ---------- helpers ----------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
