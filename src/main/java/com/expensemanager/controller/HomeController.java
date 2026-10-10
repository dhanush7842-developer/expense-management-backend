package com.expensemanager.controller;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public HomeController(UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          ExpenseRepository expenseRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    // Default JSON view showing full live database data and statistics
    @GetMapping(value = {"/", "/api/data"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new LinkedHashMap<>();

        // Server info
        response.put("status", "UP");
        response.put("application", "Smart Expense Management System - Backend");
        response.put("port", 8080);
        response.put("database", "MySQL (expense_manager_db) - Connected");
        response.put("message", "Backend is running and showing live database records!");
        response.put("viewHtmlUrl", "http://localhost:8080/view");

        // Live Statistics
        long totalUsers = userRepository.count();
        long totalCategories = categoryRepository.count();
        long totalExpenses = expenseRepository.count();
        BigDecimal totalExpenseAmount = expenseRepository.sumAllExpenses();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUsers", totalUsers);
        summary.put("totalCategories", totalCategories);
        summary.put("totalExpenses", totalExpenses);
        summary.put("totalExpenseAmount", totalExpenseAmount);
        response.put("summary", summary);

        // Users data (safe - never expose password hashes)
        List<Map<String, Object>> usersList = userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("email", u.getEmail());
            map.put("role", u.getRole());
            map.put("createdAt", u.getCreatedAt());
            return map;
        }).toList();
        response.put("users", usersList);

        // Expenses data with category & user details
        List<Map<String, Object>> expensesList = expenseRepository.findAllWithDetails().stream().map(e -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getTitle());
            map.put("amount", e.getAmount());
            map.put("expenseDate", e.getExpenseDate());
            map.put("category", e.getCategory() != null ? e.getCategory().getName() : null);
            map.put("userEmail", e.getUser() != null ? e.getUser().getEmail() : null);
            map.put("userName", e.getUser() != null ? e.getUser().getName() : null);
            map.put("description", e.getDescription());
            map.put("createdAt", e.getCreatedAt());
            return map;
        }).toList();
        response.put("expenses", expensesList);

        // Categories data
        List<Map<String, Object>> categoriesList = categoryRepository.findAllWithUser().stream().map(c -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("description", c.getDescription());
            map.put("userEmail", c.getUser() != null ? c.getUser().getEmail() : null);
            return map;
        }).toList();
        response.put("categories", categoriesList);

        // Available Endpoints list
        Map<String, Object> endpoints = new LinkedHashMap<>();
        endpoints.put("auth", List.of("POST /api/auth/register", "POST /api/auth/login"));
        endpoints.put("users", List.of("GET /api/users", "GET /api/users/me", "GET /api/users/{id}"));
        endpoints.put("expenses", List.of("GET /api/expenses", "POST /api/expenses", "PUT /api/expenses/{id}", "DELETE /api/expenses/{id}"));
        endpoints.put("categories", List.of("GET /api/categories", "POST /api/categories", "PUT /api/categories/{id}", "DELETE /api/categories/{id}"));
        endpoints.put("dashboard", List.of("GET /api/dashboard/summary", "GET /api/dashboard/category-wise", "GET /api/dashboard/monthly"));
        endpoints.put("budgets", List.of("GET /api/budgets/current", "GET /api/budgets", "PUT /api/budgets"));
        endpoints.put("savingsGoals", List.of("GET /api/savings-goals", "POST /api/savings-goals", "PUT /api/savings-goals/{id}", "DELETE /api/savings-goals/{id}", "POST /api/savings-goals/{id}/add-money"));
        endpoints.put("analytics", List.of("GET /api/analytics/summary", "GET /api/analytics/category-wise", "GET /api/analytics/daily"));
        endpoints.put("insights", List.of("GET /api/insights"));
        response.put("availableEndpoints", endpoints);

        return ResponseEntity.ok(response);
    }

    // Optional Visual HTML dashboard at http://localhost:8080/view
    @GetMapping(value = "/view", produces = MediaType.TEXT_HTML_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<String> htmlView() {
        long userCount = userRepository.count();
        long catCount = categoryRepository.count();
        long expCount = expenseRepository.count();
        BigDecimal totalSpent = expenseRepository.sumAllExpenses();

        List<User> users = userRepository.findAll();
        List<Expense> expenses = expenseRepository.findAllWithDetails();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Backend Data Explorer</title>");
        sb.append("<style>");
        sb.append("body{font-family:Segoe UI,sans-serif;background:#0f172a;color:#f8fafc;margin:0;padding:24px;}");
        sb.append("h1,h2{color:#38bdf8;margin-bottom:8px;}");
        sb.append(".header{display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid #334155;padding-bottom:16px;}");
        sb.append(".badge{background:#10b981;color:#fff;padding:4px 10px;border-radius:12px;font-size:13px;}");
        sb.append(".grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:16px;margin:24px 0;}");
        sb.append(".card{background:#1e293b;border-radius:10px;padding:20px;box-shadow:0 4px 6px -1px rgba(0,0,0,0.3);border:1px solid #334155;}");
        sb.append(".card h3{margin:0 0 8px 0;font-size:14px;color:#94a3b8;text-transform:uppercase;}");
        sb.append(".card .val{font-size:26px;font-weight:bold;color:#f8fafc;}");
        sb.append("table{width:100%;border-collapse:collapse;margin-top:12px;background:#1e293b;border-radius:8px;overflow:hidden;}");
        sb.append("th,td{padding:12px;text-align:left;border-bottom:1px solid #334155;font-size:14px;}");
        sb.append("th{background:#334155;color:#94a3b8;}");
        sb.append("tr:hover{background:#293548;}");
        sb.append("a.btn{display:inline-block;padding:8px 16px;background:#38bdf8;color:#0f172a;font-weight:600;text-decoration:none;border-radius:6px;margin-top:12px;}");
        sb.append("</style></head><body>");

        sb.append("<div class='header'><div><h1>Smart Expense Management System</h1><p>Backend Live Data & MySQL Records</p></div>");
        sb.append("<div><span class='badge'>Status: UP (Port 8080)</span> <a class='btn' href='/'>Raw JSON View</a></div></div>");

        sb.append("<div class='grid'>");
        sb.append("<div class='card'><h3>Total Users</h3><div class='val'>").append(userCount).append("</div></div>");
        sb.append("<div class='card'><h3>Total Categories</h3><div class='val'>").append(catCount).append("</div></div>");
        sb.append("<div class='card'><h3>Total Expenses</h3><div class='val'>").append(expCount).append("</div></div>");
        sb.append("<div class='card'><h3>Total Amount Spent</h3><div class='val'>&#8377; ").append(totalSpent).append("</div></div>");
        sb.append("</div>");

        // Users Table
        sb.append("<h2>Registered Users</h2>");
        sb.append("<table><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Created At</th></tr>");
        for (User u : users) {
            sb.append("<tr><td>").append(u.getId()).append("</td>")
              .append("<td>").append(u.getName()).append("</td>")
              .append("<td>").append(u.getEmail()).append("</td>")
              .append("<td>").append(u.getRole()).append("</td>")
              .append("<td>").append(u.getCreatedAt()).append("</td></tr>");
        }
        sb.append("</table>");

        // Expenses Table
        sb.append("<h2 style='margin-top:32px;'>Recent Expenses</h2>");
        sb.append("<table><tr><th>ID</th><th>Title</th><th>Amount</th><th>Category</th><th>User</th><th>Date</th><th>Description</th></tr>");
        for (Expense e : expenses) {
            sb.append("<tr><td>").append(e.getId()).append("</td>")
              .append("<td>").append(e.getTitle()).append("</td>")
              .append("<td>&#8377; ").append(e.getAmount()).append("</td>")
              .append("<td>").append(e.getCategory() != null ? e.getCategory().getName() : "-").append("</td>")
              .append("<td>").append(e.getUser() != null ? e.getUser().getEmail() : "-").append("</td>")
              .append("<td>").append(e.getExpenseDate()).append("</td>")
              .append("<td>").append(e.getDescription() != null ? e.getDescription() : "-").append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("</body></html>");

        return ResponseEntity.ok(sb.toString());
    }
}
