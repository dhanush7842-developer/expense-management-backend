package com.expensemanager.repository;

import com.expensemanager.dto.CategoryTotalResponse;
import com.expensemanager.dto.DailyTotalResponse;
import com.expensemanager.dto.ExpenseStats;
import com.expensemanager.dto.MonthlyTotalResponse;
import com.expensemanager.entity.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

        // ---------- CRUD helpers ----------

        // @EntityGraph loads the category together with the expense (avoids
        // lazy-loading errors)
        @EntityGraph(attributePaths = "category")
        List<Expense> findByUserIdOrderByExpenseDateDescIdDesc(Long userId);

        @EntityGraph(attributePaths = "category")
        Optional<Expense> findByIdAndUserId(Long id, Long userId);

        // Used to stop deleting a category that still has expenses
        boolean existsByCategoryId(Long categoryId);

        long countByUserId(Long userId);

        // ---------- Dashboard queries ----------

        // Total of all expenses (can return null if the user has no expenses)
        @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
        BigDecimal sumTotalByUserId(@Param("userId") Long userId);

        // Total between two dates (used for the current month)
        @Query("SELECT SUM(e.amount) FROM Expense e " +
                        "WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
        BigDecimal sumByUserIdAndDateRange(@Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Category-wise totals, highest first
        @Query("SELECT new com.expensemanager.dto.CategoryTotalResponse(c.name, SUM(e.amount)) " +
                        "FROM Expense e JOIN e.category c " +
                        "WHERE e.user.id = :userId " +
                        "GROUP BY c.name " +
                        "ORDER BY SUM(e.amount) DESC")
        List<CategoryTotalResponse> findCategoryTotals(@Param("userId") Long userId);

        // Month-wise totals for one year, January to December
        @Query("SELECT new com.expensemanager.dto.MonthlyTotalResponse(" +
                        "YEAR(e.expenseDate), MONTH(e.expenseDate), SUM(e.amount)) " +
                        "FROM Expense e " +
                        "WHERE e.user.id = :userId AND YEAR(e.expenseDate) = :year " +
                        "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
                        "ORDER BY MONTH(e.expenseDate)")
        List<MonthlyTotalResponse> findMonthlyTotals(@Param("userId") Long userId,
                        @Param("year") int year);

        // ---------- Analytics queries (added for Expense Analytics) ----------

        // Count, total, highest and lowest expense between two dates (always returns
        // one row)
        @Query("SELECT new com.expensemanager.dto.ExpenseStats(COUNT(e), SUM(e.amount), MAX(e.amount), MIN(e.amount)) "
                        +
                        "FROM Expense e " +
                        "WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate")
        ExpenseStats findStatsByUserIdAndDateRange(@Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Category-wise totals between two dates, highest first
        @Query("SELECT new com.expensemanager.dto.CategoryTotalResponse(c.name, SUM(e.amount)) " +
                        "FROM Expense e JOIN e.category c " +
                        "WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY c.name " +
                        "ORDER BY SUM(e.amount) DESC")
        List<CategoryTotalResponse> findCategoryTotalsByDateRange(@Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Day-wise totals between two dates (only days that have expenses)
        @Query("SELECT new com.expensemanager.dto.DailyTotalResponse(e.expenseDate, SUM(e.amount)) " +
                        "FROM Expense e " +
                        "WHERE e.user.id = :userId AND e.expenseDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.expenseDate " +
                        "ORDER BY e.expenseDate")
        List<DailyTotalResponse> findDailyTotals(@Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Global sum of all expenses across all users (used by HomeController)
        @Query("SELECT SUM(e.amount) FROM Expense e")
        BigDecimal sumAllExpenses();

        // Fetch all expenses with category and user (used by HomeController)
        @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category LEFT JOIN FETCH e.user ORDER BY e.expenseDate DESC, e.id DESC")
        List<Expense> findAllWithDetails();
}
