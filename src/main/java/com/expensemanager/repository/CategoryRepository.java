package com.expensemanager.repository;

import com.expensemanager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // All categories of one user, sorted A-Z
    List<Category> findByUserIdOrderByNameAsc(Long userId);

    // Find a category only if it belongs to this user
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    // Duplicate name check when adding
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    // Duplicate name check when updating (ignore the same category)
    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, Long userId, Long id);

    // Fetch all categories with user
    @Query("SELECT c FROM Category c JOIN FETCH c.user ORDER BY c.id ASC")
    List<Category> findAllWithUser();
}
