package com.example.employee_api.controller;

import com.example.employee_api.model.Goal;
import com.example.employee_api.model.PerformanceReview;
import com.example.employee_api.model.enums.GoalStatus;
import com.example.employee_api.model.enums.ReviewStatus;
import com.example.employee_api.service.PerformanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/performance")
@CrossOrigin(origins = "*")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    // ===== PERFORMANCE REVIEW ENDPOINTS =====

    // Get all performance reviews
    @GetMapping("/reviews")
    public ResponseEntity<List<PerformanceReview>> getAllPerformanceReviews() {
        List<PerformanceReview> reviews = performanceService.getAllPerformanceReviews();
        return ResponseEntity.ok(reviews);
    }

    // Get performance reviews with pagination
    @GetMapping("/reviews/paginated")
    public ResponseEntity<Page<PerformanceReview>> getAllPerformanceReviewsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PerformanceReview> reviews = performanceService.getAllPerformanceReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    // Get performance review by ID
    @GetMapping("/reviews/{id}")
    public ResponseEntity<PerformanceReview> getPerformanceReviewById(@PathVariable Long id) {
        PerformanceReview review = performanceService.getPerformanceReviewById(id);
        return ResponseEntity.ok(review);
    }

    // Create new performance review
    @PostMapping("/reviews")
    public ResponseEntity<PerformanceReview> createPerformanceReview(@Valid @RequestBody PerformanceReview review) {
        PerformanceReview createdReview = performanceService.createPerformanceReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    // Update performance review
    @PutMapping("/reviews/{id}")
    public ResponseEntity<PerformanceReview> updatePerformanceReview(
            @PathVariable Long id, 
            @Valid @RequestBody PerformanceReview reviewDetails) {
        PerformanceReview updatedReview = performanceService.updatePerformanceReview(id, reviewDetails);
        return ResponseEntity.ok(updatedReview);
    }

    // Delete performance review
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deletePerformanceReview(@PathVariable Long id) {
        performanceService.deletePerformanceReview(id);
        return ResponseEntity.noContent().build();
    }

    // Get performance reviews by employee
    @GetMapping("/reviews/employee/{employeeId}")
    public ResponseEntity<List<PerformanceReview>> getPerformanceReviewsByEmployee(@PathVariable Long employeeId) {
        List<PerformanceReview> reviews = performanceService.getPerformanceReviewsByEmployee(employeeId);
        return ResponseEntity.ok(reviews);
    }

    // Get performance reviews by reviewer
    @GetMapping("/reviews/reviewer/{reviewerId}")
    public ResponseEntity<List<PerformanceReview>> getPerformanceReviewsByReviewer(@PathVariable Long reviewerId) {
        List<PerformanceReview> reviews = performanceService.getPerformanceReviewsByReviewer(reviewerId);
        return ResponseEntity.ok(reviews);
    }

    // Get performance reviews by status
    @GetMapping("/reviews/status/{status}")
    public ResponseEntity<List<PerformanceReview>> getPerformanceReviewsByStatus(@PathVariable ReviewStatus status) {
        List<PerformanceReview> reviews = performanceService.getPerformanceReviewsByStatus(status);
        return ResponseEntity.ok(reviews);
    }

    // Get overdue performance reviews
    @GetMapping("/reviews/overdue")
    public ResponseEntity<List<PerformanceReview>> getOverduePerformanceReviews() {
        List<PerformanceReview> reviews = performanceService.getOverduePerformanceReviews();
        return ResponseEntity.ok(reviews);
    }

    // Get performance reviews in period
    @GetMapping("/reviews/period")
    public ResponseEntity<List<PerformanceReview>> getPerformanceReviewsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PerformanceReview> reviews = performanceService.getPerformanceReviewsInPeriod(startDate, endDate);
        return ResponseEntity.ok(reviews);
    }

    // Submit review for approval
    @PutMapping("/reviews/{id}/submit")
    public ResponseEntity<PerformanceReview> submitReview(@PathVariable Long id) {
        PerformanceReview review = performanceService.submitReview(id);
        return ResponseEntity.ok(review);
    }

    // Approve review
    @PutMapping("/reviews/{id}/approve")
    public ResponseEntity<PerformanceReview> approveReview(@PathVariable Long id) {
        PerformanceReview review = performanceService.approveReview(id);
        return ResponseEntity.ok(review);
    }

    // Start review
    @PutMapping("/reviews/{id}/start")
    public ResponseEntity<PerformanceReview> startReview(@PathVariable Long id) {
        PerformanceReview review = performanceService.startReview(id);
        return ResponseEntity.ok(review);
    }

    // Complete review
    @PutMapping("/reviews/{id}/complete")
    public ResponseEntity<PerformanceReview> completeReview(@PathVariable Long id) {
        PerformanceReview review = performanceService.completeReview(id);
        return ResponseEntity.ok(review);
    }

    // ===== GOAL ENDPOINTS =====

    // Get all goals
    @GetMapping("/goals")
    public ResponseEntity<List<Goal>> getAllGoals() {
        List<Goal> goals = performanceService.getAllGoals();
        return ResponseEntity.ok(goals);
    }

    // Get goals with pagination
    @GetMapping("/goals/paginated")
    public ResponseEntity<Page<Goal>> getAllGoalsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Goal> goals = performanceService.getAllGoals(pageable);
        return ResponseEntity.ok(goals);
    }

    // Get goal by ID
    @GetMapping("/goals/{id}")
    public ResponseEntity<Goal> getGoalById(@PathVariable Long id) {
        Goal goal = performanceService.getGoalById(id);
        return ResponseEntity.ok(goal);
    }

    // Create new goal
    @PostMapping("/goals")
    public ResponseEntity<Goal> createGoal(@Valid @RequestBody Goal goal) {
        Goal createdGoal = performanceService.createGoal(goal);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
    }

    // Update goal
    @PutMapping("/goals/{id}")
    public ResponseEntity<Goal> updateGoal(@PathVariable Long id, @Valid @RequestBody Goal goalDetails) {
        Goal updatedGoal = performanceService.updateGoal(id, goalDetails);
        return ResponseEntity.ok(updatedGoal);
    }

    // Delete goal
    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        performanceService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    // Get goals by employee
    @GetMapping("/goals/employee/{employeeId}")
    public ResponseEntity<List<Goal>> getGoalsByEmployee(@PathVariable Long employeeId) {
        List<Goal> goals = performanceService.getGoalsByEmployee(employeeId);
        return ResponseEntity.ok(goals);
    }

    // Get active goals by employee
    @GetMapping("/goals/employee/{employeeId}/active")
    public ResponseEntity<List<Goal>> getActiveGoalsByEmployee(@PathVariable Long employeeId) {
        List<Goal> goals = performanceService.getActiveGoalsByEmployee(employeeId);
        return ResponseEntity.ok(goals);
    }

    // Get completed goals by employee
    @GetMapping("/goals/employee/{employeeId}/completed")
    public ResponseEntity<List<Goal>> getCompletedGoalsByEmployee(@PathVariable Long employeeId) {
        List<Goal> goals = performanceService.getCompletedGoalsByEmployee(employeeId);
        return ResponseEntity.ok(goals);
    }

    // Get goals by status
    @GetMapping("/goals/status/{status}")
    public ResponseEntity<List<Goal>> getGoalsByStatus(@PathVariable GoalStatus status) {
        List<Goal> goals = performanceService.getGoalsByStatus(status);
        return ResponseEntity.ok(goals);
    }

    // Get overdue goals
    @GetMapping("/goals/overdue")
    public ResponseEntity<List<Goal>> getOverdueGoals() {
        List<Goal> goals = performanceService.getOverdueGoals();
        return ResponseEntity.ok(goals);
    }

    // Get goals due in next X days
    @GetMapping("/goals/due-soon")
    public ResponseEntity<List<Goal>> getGoalsDueInNextDays(@RequestParam(defaultValue = "7") int days) {
        List<Goal> goals = performanceService.getGoalsDueInNextDays(days);
        return ResponseEntity.ok(goals);
    }

    // Get high priority goals by employee
    @GetMapping("/goals/employee/{employeeId}/high-priority")
    public ResponseEntity<List<Goal>> getHighPriorityGoalsByEmployee(@PathVariable Long employeeId) {
        List<Goal> goals = performanceService.getHighPriorityGoalsByEmployee(employeeId);
        return ResponseEntity.ok(goals);
    }

    // Update goal progress
    @PutMapping("/goals/{id}/progress")
    public ResponseEntity<Goal> updateGoalProgress(
            @PathVariable Long id, 
            @RequestParam BigDecimal progressPercentage) {
        Goal goal = performanceService.updateGoalProgress(id, progressPercentage);
        return ResponseEntity.ok(goal);
    }

    // Complete goal
    @PutMapping("/goals/{id}/complete")
    public ResponseEntity<Goal> completeGoal(@PathVariable Long id) {
        Goal goal = performanceService.completeGoal(id);
        return ResponseEntity.ok(goal);
    }

    // Start goal
    @PutMapping("/goals/{id}/start")
    public ResponseEntity<Goal> startGoal(@PathVariable Long id) {
        Goal goal = performanceService.startGoal(id);
        return ResponseEntity.ok(goal);
    }

    // Pause goal
    @PutMapping("/goals/{id}/pause")
    public ResponseEntity<Goal> pauseGoal(@PathVariable Long id) {
        Goal goal = performanceService.pauseGoal(id);
        return ResponseEntity.ok(goal);
    }

    // Cancel goal
    @PutMapping("/goals/{id}/cancel")
    public ResponseEntity<Goal> cancelGoal(@PathVariable Long id) {
        Goal goal = performanceService.cancelGoal(id);
        return ResponseEntity.ok(goal);
    }

    // ===== STATISTICS AND REPORTING ENDPOINTS =====

    // Get average progress by employee
    @GetMapping("/goals/employee/{employeeId}/average-progress")
    public ResponseEntity<Double> getAverageProgressByEmployee(@PathVariable Long employeeId) {
        Double averageProgress = performanceService.getAverageProgressByEmployee(employeeId);
        return ResponseEntity.ok(averageProgress);
    }

    // Get average progress by department
    @GetMapping("/goals/department/{departmentId}/average-progress")
    public ResponseEntity<Double> getAverageProgressByDepartment(@PathVariable Long departmentId) {
        Double averageProgress = performanceService.getAverageProgressByDepartment(departmentId);
        return ResponseEntity.ok(averageProgress);
    }

    // Get completed goals count by employee in period
    @GetMapping("/goals/employee/{employeeId}/completed-count")
    public ResponseEntity<Long> getCompletedGoalsCountByEmployee(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        long count = performanceService.getCompletedGoalsCountByEmployee(employeeId, startDate, endDate);
        return ResponseEntity.ok(count);
    }

    // Get most recent goal by employee
    @GetMapping("/goals/employee/{employeeId}/most-recent")
    public ResponseEntity<Goal> getMostRecentGoalByEmployee(@PathVariable Long employeeId) {
        Optional<Goal> goal = performanceService.getMostRecentGoalByEmployee(employeeId);
        return goal.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    // ===== VALIDATION ENDPOINTS =====

    // Check if employee can create goal
    @GetMapping("/goals/employee/{employeeId}/can-create")
    public ResponseEntity<Boolean> canEmployeeCreateGoal(@PathVariable Long employeeId) {
        boolean canCreate = performanceService.canEmployeeCreateGoal(employeeId);
        return ResponseEntity.ok(canCreate);
    }

    // Check if review can be started
    @GetMapping("/reviews/{reviewId}/can-start")
    public ResponseEntity<Boolean> canStartReview(@PathVariable Long reviewId) {
        boolean canStart = performanceService.canStartReview(reviewId);
        return ResponseEntity.ok(canStart);
    }

    // Check if review can be completed
    @GetMapping("/reviews/{reviewId}/can-complete")
    public ResponseEntity<Boolean> canCompleteReview(@PathVariable Long reviewId) {
        boolean canComplete = performanceService.canCompleteReview(reviewId);
        return ResponseEntity.ok(canComplete);
    }
}