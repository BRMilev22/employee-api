package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Goal;
import com.example.employee_api.model.PerformanceReview;
import com.example.employee_api.model.enums.GoalStatus;
import com.example.employee_api.model.enums.ReviewStatus;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.GoalRepository;
import com.example.employee_api.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PerformanceService {

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private NotificationService notificationService;

    // PerformanceReview CRUD operations
    public PerformanceReview createPerformanceReview(PerformanceReview review) {
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        PerformanceReview savedReview = performanceReviewRepository.save(review);
        
        // Send notification to employee about new performance review
        try {
            if (review.getReviewer() != null) {
                notificationService.sendPerformanceReviewNotification(
                    savedReview.getId(),
                    review.getEmployee().getId(),
                    review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName()
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the review creation
            System.err.println("Failed to send performance review notification: " + e.getMessage());
        }
        
        return savedReview;
    }

    public PerformanceReview updatePerformanceReview(Long id, PerformanceReview reviewDetails) {
        PerformanceReview review = getPerformanceReviewById(id);
        
        // Update basic fields
        review.setTitle(reviewDetails.getTitle());
        review.setDescription(reviewDetails.getDescription());
        review.setReviewPeriodStart(reviewDetails.getReviewPeriodStart());
        review.setReviewPeriodEnd(reviewDetails.getReviewPeriodEnd());
        review.setDueDate(reviewDetails.getDueDate());
        review.setStatus(reviewDetails.getStatus());
        
        // Update ratings
        review.setOverallRating(reviewDetails.getOverallRating());
        review.setOverallScore(reviewDetails.getOverallScore());
        
        // Update comments
        review.setEmployeeComments(reviewDetails.getEmployeeComments());
        review.setManagerComments(reviewDetails.getManagerComments());
        review.setHrComments(reviewDetails.getHrComments());
        review.setStrengths(reviewDetails.getStrengths());
        review.setAreasForImprovement(reviewDetails.getAreasForImprovement());
        review.setDevelopmentGoals(reviewDetails.getDevelopmentGoals());
        
        review.setUpdatedAt(LocalDateTime.now());
        return performanceReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public PerformanceReview getPerformanceReviewById(Long id) {
        return performanceReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Performance Review not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PerformanceReview> getAllPerformanceReviews() {
        return performanceReviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<PerformanceReview> getAllPerformanceReviews(Pageable pageable) {
        return performanceReviewRepository.findAll(pageable);
    }

    public void deletePerformanceReview(Long id) {
        if (!performanceReviewRepository.existsById(id)) {
            throw new RuntimeException("Performance Review not found with id: " + id);
        }
        performanceReviewRepository.deleteById(id);
    }

    // Performance Review business methods
    @Transactional(readOnly = true)
    public List<PerformanceReview> getPerformanceReviewsByEmployee(Long employeeId) {
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<PerformanceReview> getPerformanceReviewsByReviewer(Long reviewerId) {
        return performanceReviewRepository.findByReviewerId(reviewerId);
    }

    @Transactional(readOnly = true)
    public List<PerformanceReview> getPerformanceReviewsByStatus(ReviewStatus status) {
        return performanceReviewRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<PerformanceReview> getOverduePerformanceReviews() {
        return performanceReviewRepository.findOverdueReviews(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<PerformanceReview> getPerformanceReviewsInPeriod(LocalDate startDate, LocalDate endDate) {
        return performanceReviewRepository.findByReviewPeriodBetween(startDate, endDate);
    }

    public PerformanceReview submitReview(Long reviewId) {
        PerformanceReview review = getPerformanceReviewById(reviewId);
        review.setStatus(ReviewStatus.PENDING);
        review.setUpdatedAt(LocalDateTime.now());
        return performanceReviewRepository.save(review);
    }

    public PerformanceReview approveReview(Long reviewId) {
        PerformanceReview review = getPerformanceReviewById(reviewId);
        review.setStatus(ReviewStatus.APPROVED);
        review.setUpdatedAt(LocalDateTime.now());
        return performanceReviewRepository.save(review);
    }

    public PerformanceReview startReview(Long reviewId) {
        PerformanceReview review = getPerformanceReviewById(reviewId);
        review.setStatus(ReviewStatus.IN_PROGRESS);
        review.setUpdatedAt(LocalDateTime.now());
        return performanceReviewRepository.save(review);
    }

    public PerformanceReview completeReview(Long reviewId) {
        PerformanceReview review = getPerformanceReviewById(reviewId);
        review.setStatus(ReviewStatus.COMPLETED);
        review.setCompletedDate(LocalDate.now());
        review.setUpdatedAt(LocalDateTime.now());
        return performanceReviewRepository.save(review);
    }

    // Goal CRUD operations
    public Goal createGoal(Goal goal) {
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }

    public Goal updateGoal(Long id, Goal goalDetails) {
        Goal goal = getGoalById(id);
        
        goal.setTitle(goalDetails.getTitle());
        goal.setDescription(goalDetails.getDescription());
        goal.setStartDate(goalDetails.getStartDate());
        goal.setDueDate(goalDetails.getDueDate());
        goal.setProgressPercentage(goalDetails.getProgressPercentage());
        goal.setPriority(goalDetails.getPriority());
        goal.setStatus(goalDetails.getStatus());
        goal.setSuccessCriteria(goalDetails.getSuccessCriteria());
        goal.setNotes(goalDetails.getNotes());
        
        goal.setUpdatedAt(LocalDateTime.now());
        
        // Update completion date if status is completed
        if (goalDetails.getStatus() == GoalStatus.COMPLETED && goal.getCompletedDate() == null) {
            goal.setCompletedDate(LocalDate.now());
        } else if (goalDetails.getStatus() != GoalStatus.COMPLETED) {
            goal.setCompletedDate(null);
        }
        
        return goalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public Goal getGoalById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Goal> getAllGoals(Pageable pageable) {
        return goalRepository.findAll(pageable);
    }

    public void deleteGoal(Long id) {
        if (!goalRepository.existsById(id)) {
            throw new RuntimeException("Goal not found with id: " + id);
        }
        goalRepository.deleteById(id);
    }

    // Goal business methods
    @Transactional(readOnly = true)
    public List<Goal> getGoalsByEmployee(Long employeeId) {
        return goalRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Goal> getActiveGoalsByEmployee(Long employeeId) {
        return goalRepository.findActiveGoalsByEmployee(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Goal> getCompletedGoalsByEmployee(Long employeeId) {
        return goalRepository.findCompletedGoalsByEmployee(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Goal> getGoalsByStatus(GoalStatus status) {
        return goalRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Goal> getOverdueGoals() {
        return goalRepository.findOverdueGoals(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Goal> getGoalsDueInNextDays(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return goalRepository.findGoalsDueInPeriod(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Goal> getHighPriorityGoalsByEmployee(Long employeeId) {
        return goalRepository.findHighPriorityGoalsByEmployee(employeeId);
    }

    public Goal updateGoalProgress(Long goalId, BigDecimal progressPercentage) {
        Goal goal = getGoalById(goalId);
        goal.setProgressPercentage(progressPercentage);
        goal.setUpdatedAt(LocalDateTime.now());
        
        // Auto-complete if 100% progress
        if (progressPercentage.compareTo(BigDecimal.valueOf(100)) >= 0 && goal.getStatus() != GoalStatus.COMPLETED) {
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setCompletedDate(LocalDate.now());
        }
        
        return goalRepository.save(goal);
    }

    public Goal completeGoal(Long goalId) {
        Goal goal = getGoalById(goalId);
        goal.setStatus(GoalStatus.COMPLETED);
        goal.setProgressPercentage(BigDecimal.valueOf(100));
        goal.setCompletedDate(LocalDate.now());
        goal.setUpdatedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }

    public Goal startGoal(Long goalId) {
        Goal goal = getGoalById(goalId);
        goal.setStatus(GoalStatus.IN_PROGRESS);
        goal.setUpdatedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }

    public Goal pauseGoal(Long goalId) {
        Goal goal = getGoalById(goalId);
        goal.setStatus(GoalStatus.ON_HOLD);
        goal.setUpdatedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }

    public Goal cancelGoal(Long goalId) {
        Goal goal = getGoalById(goalId);
        goal.setStatus(GoalStatus.CANCELLED);
        goal.setUpdatedAt(LocalDateTime.now());
        return goalRepository.save(goal);
    }

    // Statistics and reporting methods
    @Transactional(readOnly = true)
    public Double getAverageProgressByEmployee(Long employeeId) {
        return goalRepository.getAverageProgressByEmployee(employeeId);
    }

    @Transactional(readOnly = true)
    public Double getAverageProgressByDepartment(Long departmentId) {
        return goalRepository.getAverageProgressByDepartment(departmentId);
    }

    @Transactional(readOnly = true)
    public long getCompletedGoalsCountByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return goalRepository.countCompletedGoalsByEmployeeInPeriod(employeeId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Optional<Goal> getMostRecentGoalByEmployee(Long employeeId) {
        return goalRepository.findMostRecentGoalByEmployee(employeeId);
    }

    // Validation helper methods
    private void validateEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException("Employee not found with id: " + employeeId);
        }
    }

    // Business validation methods
    public boolean canEmployeeCreateGoal(Long employeeId) {
        validateEmployee(employeeId);
        // Business logic: Check if employee has too many active goals
        List<Goal> activeGoals = getActiveGoalsByEmployee(employeeId);
        return activeGoals.size() < 10; // Maximum 10 active goals per employee
    }

    public boolean canStartReview(Long reviewId) {
        PerformanceReview review = getPerformanceReviewById(reviewId);
        return review.getStatus() == ReviewStatus.DRAFT || review.getStatus() == ReviewStatus.PENDING;
    }

    public boolean canCompleteReview(Long reviewId) {
        PerformanceReview review = getPerformanceReviewById(reviewId);
        return review.getStatus() == ReviewStatus.IN_PROGRESS;
    }
}