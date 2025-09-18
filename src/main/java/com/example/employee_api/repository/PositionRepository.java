package com.example.employee_api.repository;

import com.example.employee_api.model.Position;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Position entity operations
 * Provides data access methods for managing positions/job titles
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    /**
     * Find positions by title (case-insensitive)
     */
    List<Position> findByTitleContainingIgnoreCase(String title);

    /**
     * Find positions by department ID
     */
    List<Position> findByDepartmentId(Long departmentId);

    /**
     * Find positions by level
     */
    List<Position> findByLevel(PositionLevel level);

    /**
     * Find positions by status
     */
    List<Position> findByStatus(PositionStatus status);

    /**
     * Find active positions
     */
    List<Position> findByStatusOrderByTitleAsc(PositionStatus status);

    /**
     * Find positions with open headcount
     */
    @Query("SELECT p FROM Position p WHERE p.numberOfOpenings > 0 AND p.status = 'ACTIVE'")
    List<Position> findPositionsWithOpenings();

    /**
     * Find positions by salary range
     */
    @Query("SELECT p FROM Position p WHERE p.minSalary >= :minSalary AND p.maxSalary <= :maxSalary")
    List<Position> findBySalaryRange(@Param("minSalary") BigDecimal minSalary, @Param("maxSalary") BigDecimal maxSalary);

    /**
     * Find positions within a salary range that overlap with given range
     */
    @Query("SELECT p FROM Position p WHERE " +
           "(p.minSalary BETWEEN :minSalary AND :maxSalary) OR " +
           "(p.maxSalary BETWEEN :minSalary AND :maxSalary) OR " +
           "(p.minSalary <= :minSalary AND p.maxSalary >= :maxSalary)")
    List<Position> findPositionsWithOverlappingSalaryRange(@Param("minSalary") BigDecimal minSalary, 
                                                          @Param("maxSalary") BigDecimal maxSalary);

    /**
     * Find positions by department and level
     */
    List<Position> findByDepartmentIdAndLevel(Long departmentId, PositionLevel level);

    /**
     * Find positions that report to a specific position
     */
    List<Position> findByReportsToId(Long reportsToPositionId);

    /**
     * Find top-level positions (no reporting relationship)
     */
    List<Position> findByReportsToIsNull();

    /**
     * Count positions by department
     */
    @Query("SELECT COUNT(p) FROM Position p WHERE p.department.id = :departmentId AND p.status = 'ACTIVE'")
    Long countActivePositionsByDepartment(@Param("departmentId") Long departmentId);

    /**
     * Count positions by level
     */
    Long countByLevelAndStatus(PositionLevel level, PositionStatus status);

    /**
     * Find positions by pay grade
     */
    List<Position> findByPayGrade(String payGrade);

    /**
     * Find positions requiring specific minimum experience
     */
    @Query("SELECT p FROM Position p WHERE p.minExperienceYears <= :experienceYears AND p.status = 'ACTIVE'")
    List<Position> findPositionsForExperienceLevel(@Param("experienceYears") Integer experienceYears);

    /**
     * Find positions with specific skills requirements
     */
    @Query("SELECT p FROM Position p WHERE " +
           "LOWER(p.requiredSkills) LIKE LOWER(CONCAT('%', :skill, '%')) OR " +
           "LOWER(p.preferredSkills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Position> findPositionsRequiringSkill(@Param("skill") String skill);

    /**
     * Get position hierarchy starting from a root position
     */
    @Query("SELECT p FROM Position p WHERE p.reportsTo.id = :positionId ORDER BY p.level, p.title")
    List<Position> findDirectReports(@Param("positionId") Long positionId);

    /**
     * Find available positions (active with openings)
     */
    @Query("SELECT p FROM Position p WHERE p.status = 'ACTIVE' AND p.numberOfOpenings > 0 ORDER BY p.level, p.title")
    List<Position> findAvailablePositions();

    /**
     * Get total headcount for all positions in a department
     */
    @Query("SELECT COALESCE(SUM(p.totalHeadcount), 0) FROM Position p WHERE p.department.id = :departmentId AND p.status = 'ACTIVE'")
    Integer getTotalHeadcountByDepartment(@Param("departmentId") Long departmentId);

    /**
     * Get total openings for all positions in a department
     */
    @Query("SELECT COALESCE(SUM(p.numberOfOpenings), 0) FROM Position p WHERE p.department.id = :departmentId AND p.status = 'ACTIVE'")
    Integer getTotalOpeningsByDepartment(@Param("departmentId") Long departmentId);

    /**
     * Find positions by title exact match
     */
    Optional<Position> findByTitleAndDepartmentId(String title, Long departmentId);

    /**
     * Check if position title exists in department
     */
    boolean existsByTitleAndDepartmentId(String title, Long departmentId);

    /**
     * Find entry-level positions for a department
     */
    @Query("SELECT p FROM Position p WHERE p.department.id = :departmentId AND " +
           "p.level IN ('ENTRY', 'JUNIOR') AND p.status = 'ACTIVE' ORDER BY p.minExperienceYears")
    List<Position> findEntryLevelPositionsByDepartment(@Param("departmentId") Long departmentId);

    /**
     * Find management positions
     */
    @Query("SELECT p FROM Position p WHERE p.level IN ('MANAGER', 'SENIOR_MANAGER', 'DIRECTOR', 'VP', 'EXECUTIVE') " +
           "AND p.status = 'ACTIVE' ORDER BY p.level DESC, p.title")
    List<Position> findManagementPositions();

    /**
     * Find positions with no current employees
     */
    @Query("SELECT p FROM Position p WHERE p.currentEmployees IS EMPTY AND p.status = 'ACTIVE'")
    List<Position> findVacantPositions();
}