package com.example.employee_api.repository;

import com.example.employee_api.model.PayGrade;
import com.example.employee_api.model.enums.PayGradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PayGrade entity
 */
@Repository
public interface PayGradeRepository extends JpaRepository<PayGrade, Long> {

    /**
     * Find pay grade by grade code
     */
    Optional<PayGrade> findByGradeCode(String gradeCode);

    /**
     * Find pay grades by status
     */
    List<PayGrade> findByStatus(PayGradeStatus status);

    /**
     * Find active pay grades ordered by grade level
     */
    List<PayGrade> findByStatusOrderByGradeLevel(PayGradeStatus status);

    /**
     * Find pay grades by grade level
     */
    List<PayGrade> findByGradeLevel(Integer gradeLevel);

    /**
     * Find pay grades within salary range
     */
    @Query("SELECT pg FROM PayGrade pg WHERE pg.minSalary <= :salary AND pg.maxSalary >= :salary")
    List<PayGrade> findByContainingSalary(@Param("salary") BigDecimal salary);

    /**
     * Find pay grades by minimum salary range
     */
    List<PayGrade> findByMinSalaryBetween(BigDecimal minSalary, BigDecimal maxSalary);

    /**
     * Find pay grades by maximum salary range
     */
    List<PayGrade> findByMaxSalaryBetween(BigDecimal minSalary, BigDecimal maxSalary);

    /**
     * Find pay grades with salary range overlap
     */
    @Query("SELECT pg FROM PayGrade pg WHERE " +
           "(pg.minSalary <= :maxSalary AND pg.maxSalary >= :minSalary)")
    List<PayGrade> findByOverlappingSalaryRange(@Param("minSalary") BigDecimal minSalary, 
                                                @Param("maxSalary") BigDecimal maxSalary);

    /**
     * Find pay grades by grade name containing (case-insensitive)
     */
    List<PayGrade> findByGradeNameContainingIgnoreCase(String gradeName);

    /**
     * Count active pay grades
     */
    long countByStatus(PayGradeStatus status);

    /**
     * Check if grade code exists
     */
    boolean existsByGradeCode(String gradeCode);

    /**
     * Check if grade code exists excluding specific ID
     */
    @Query("SELECT COUNT(pg) > 0 FROM PayGrade pg WHERE pg.gradeCode = :gradeCode AND pg.id != :id")
    boolean existsByGradeCodeAndIdNot(@Param("gradeCode") String gradeCode, @Param("id") Long id);

    /**
     * Find pay grades with employees count
     */
    @Query("SELECT pg, COUNT(e) as employeeCount FROM PayGrade pg " +
           "LEFT JOIN pg.employees e " +
           "WHERE pg.status = :status " +
           "GROUP BY pg " +
           "ORDER BY pg.gradeLevel")
    List<Object[]> findPayGradesWithEmployeeCount(@Param("status") PayGradeStatus status);

    /**
     * Find pay grades above certain grade level
     */
    List<PayGrade> findByGradeLevelGreaterThanOrderByGradeLevel(Integer gradeLevel);

    /**
     * Find pay grades below certain grade level
     */
    List<PayGrade> findByGradeLevelLessThanOrderByGradeLevel(Integer gradeLevel);

    /**
     * Find pay grades between grade levels
     */
    List<PayGrade> findByGradeLevelBetweenOrderByGradeLevel(Integer minLevel, Integer maxLevel);

    /**
     * Find all active pay grades with pagination
     */
    Page<PayGrade> findByStatus(PayGradeStatus status, Pageable pageable);

    /**
     * Search pay grades by grade name or description
     */
    @Query("SELECT pg FROM PayGrade pg WHERE " +
           "LOWER(pg.gradeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pg.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pg.gradeCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<PayGrade> searchPayGrades(@Param("searchTerm") String searchTerm);

    /**
     * Find pay grades suitable for salary
     */
    @Query("SELECT pg FROM PayGrade pg WHERE " +
           "pg.status = 'ACTIVE' AND " +
           "pg.minSalary <= :salary AND pg.maxSalary >= :salary " +
           "ORDER BY pg.gradeLevel")
    List<PayGrade> findSuitablePayGradesForSalary(@Param("salary") BigDecimal salary);

    /**
     * Get pay grade statistics
     */
    @Query("SELECT " +
           "COUNT(pg) as totalGrades, " +
           "AVG(pg.minSalary) as avgMinSalary, " +
           "AVG(pg.maxSalary) as avgMaxSalary, " +
           "MIN(pg.minSalary) as minSalary, " +
           "MAX(pg.maxSalary) as maxSalary " +
           "FROM PayGrade pg WHERE pg.status = :status")
    Object[] getPayGradeStatistics(@Param("status") PayGradeStatus status);
}