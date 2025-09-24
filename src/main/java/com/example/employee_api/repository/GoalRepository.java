package com.example.employee_api.repository;

import com.example.employee_api.model.Goal;
import com.example.employee_api.model.enums.GoalPriority;
import com.example.employee_api.model.enums.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long>, JpaSpecificationExecutor<Goal> {

    // Find by employee
    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId")
    List<Goal> findByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId ORDER BY g.priority DESC, g.dueDate ASC")
    Page<Goal> findByEmployeeIdOrderByPriorityDescDueDateAsc(@Param("employeeId") Long employeeId, Pageable pageable);

    // Find by performance review
    @Query("SELECT g FROM Goal g WHERE g.performanceReview.id = :reviewId")
    List<Goal> findByPerformanceReviewId(@Param("reviewId") Long reviewId);

    // Find by status
    @Query("SELECT g FROM Goal g WHERE g.status = :status ORDER BY g.priority DESC, g.dueDate ASC")
    List<Goal> findByStatus(@Param("status") GoalStatus status);

    @Query("SELECT g FROM Goal g WHERE g.status = :status ORDER BY g.priority DESC, g.dueDate ASC")
    Page<Goal> findByStatusOrderByPriorityDescDueDateAsc(@Param("status") GoalStatus status, Pageable pageable);

    // Find by priority
    @Query("SELECT g FROM Goal g WHERE g.priority = :priority ORDER BY g.dueDate ASC")
    List<Goal> findByPriority(@Param("priority") GoalPriority priority);

    // Find by employee and status
    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId AND g.status = :status ORDER BY g.priority DESC, g.dueDate ASC")
    List<Goal> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") GoalStatus status);

    // Find overdue goals
    @Query("SELECT g FROM Goal g WHERE g.dueDate < :currentDate AND g.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY g.dueDate ASC")
    List<Goal> findOverdueGoals(@Param("currentDate") LocalDate currentDate);

    // Find goals due in next X days
    @Query("SELECT g FROM Goal g WHERE g.dueDate BETWEEN :startDate AND :endDate AND g.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY g.dueDate ASC")
    List<Goal> findGoalsDueInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find active goals for employee
    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId AND g.status IN ('NOT_STARTED', 'IN_PROGRESS') ORDER BY g.priority DESC, g.dueDate ASC")
    List<Goal> findActiveGoalsByEmployee(@Param("employeeId") Long employeeId);

    // Find completed goals by employee
    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId AND g.status = 'COMPLETED' ORDER BY g.completedDate DESC")
    List<Goal> findCompletedGoalsByEmployee(@Param("employeeId") Long employeeId);

    // Find goals by date range
    @Query("SELECT g FROM Goal g WHERE g.startDate >= :startDate AND g.dueDate <= :endDate")
    List<Goal> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find goals by progress range
    @Query("SELECT g FROM Goal g WHERE g.progressPercentage >= :minProgress AND g.progressPercentage <= :maxProgress")
    List<Goal> findByProgressRange(@Param("minProgress") BigDecimal minProgress, @Param("maxProgress") BigDecimal maxProgress);

    // Find high priority goals for employee
    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId AND g.priority IN ('HIGH', 'CRITICAL') AND g.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY g.priority DESC, g.dueDate ASC")
    List<Goal> findHighPriorityGoalsByEmployee(@Param("employeeId") Long employeeId);

    // Find goals by title (partial match)
    @Query("SELECT g FROM Goal g WHERE LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Goal> findByTitleContainingIgnoreCase(@Param("title") String title);

    // Find goals without due date
    @Query("SELECT g FROM Goal g WHERE g.dueDate IS NULL")
    List<Goal> findGoalsWithoutDueDate();

    // Find goals by employee and date range
    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId AND g.startDate >= :startDate AND g.dueDate <= :endDate")
    List<Goal> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Count goals by status
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.status = :status")
    long countByStatus(@Param("status") GoalStatus status);

    // Count goals by employee and status
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.employee.id = :employeeId AND g.status = :status")
    long countByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") GoalStatus status);

    // Find goals by department (through employee)
    @Query("SELECT g FROM Goal g WHERE g.employee.department.id = :departmentId")
    List<Goal> findByEmployeeDepartmentId(@Param("departmentId") Long departmentId);

    // Statistics queries
    @Query("SELECT AVG(g.progressPercentage) FROM Goal g WHERE g.employee.id = :employeeId AND g.status IN ('IN_PROGRESS', 'COMPLETED')")
    Double getAverageProgressByEmployee(@Param("employeeId") Long employeeId);

    @Query("SELECT AVG(g.progressPercentage) FROM Goal g WHERE g.employee.department.id = :departmentId AND g.status IN ('IN_PROGRESS', 'COMPLETED')")
    Double getAverageProgressByDepartment(@Param("departmentId") Long departmentId);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.employee.id = :employeeId AND g.status = 'COMPLETED' AND g.completedDate >= :startDate AND g.completedDate <= :endDate")
    long countCompletedGoalsByEmployeeInPeriod(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find most recent goal for employee
    @Query("SELECT g FROM Goal g WHERE g.employee.id = :employeeId ORDER BY g.createdAt DESC")
    Optional<Goal> findMostRecentGoalByEmployee(@Param("employeeId") Long employeeId);
}