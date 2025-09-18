package com.example.employee_api.repository;

import com.example.employee_api.model.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LeaveType entity
 */
@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long>, JpaSpecificationExecutor<LeaveType> {

    /**
     * Find leave type by name
     */
    Optional<LeaveType> findByName(String name);

    /**
     * Find leave type by name (case-insensitive)
     */
    Optional<LeaveType> findByNameIgnoreCase(String name);

    /**
     * Check if leave type exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if leave type exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all active leave types
     */
    List<LeaveType> findByActiveTrue();

    /**
     * Find all inactive leave types
     */
    List<LeaveType> findByActiveFalse();

    /**
     * Find all active leave types ordered by display order
     */
    List<LeaveType> findByActiveTrueOrderByDisplayOrder();

    /**
     * Find all leave types ordered by display order
     */
    List<LeaveType> findAllByOrderByDisplayOrder();

    /**
     * Find leave types that apply to probation employees
     */
    List<LeaveType> findByAppliesToProbationTrueAndActiveTrue();

    /**
     * Find leave types that require approval
     */
    List<LeaveType> findByRequiresApprovalTrueAndActiveTrue();

    /**
     * Find leave types that require documents
     */
    List<LeaveType> findByRequiresDocumentsTrueAndActiveTrue();

    /**
     * Find leave types that allow carry forward
     */
    List<LeaveType> findByCarryForwardTrueAndActiveTrue();

    /**
     * Find leave types by days allowed range
     */
    List<LeaveType> findByDaysAllowedBetweenAndActiveTrue(Integer minDays, Integer maxDays);

    /**
     * Search leave types by name or description
     */
    @Query("SELECT lt FROM LeaveType lt WHERE " +
           "(LOWER(lt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "lt.active = true")
    List<LeaveType> searchActiveLeaveTypes(@Param("searchTerm") String searchTerm);

    /**
     * Search all leave types by name or description
     */
    @Query("SELECT lt FROM LeaveType lt WHERE " +
           "LOWER(lt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(lt.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<LeaveType> searchAllLeaveTypes(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find leave types with specific characteristics
     */
    @Query("SELECT lt FROM LeaveType lt WHERE " +
           "(:requiresApproval IS NULL OR lt.requiresApproval = :requiresApproval) AND " +
           "(:requiresDocuments IS NULL OR lt.requiresDocuments = :requiresDocuments) AND " +
           "(:carryForward IS NULL OR lt.carryForward = :carryForward) AND " +
           "(:appliesToProbation IS NULL OR lt.appliesToProbation = :appliesToProbation) AND " +
           "(:active IS NULL OR lt.active = :active)")
    List<LeaveType> findByCharacteristics(
            @Param("requiresApproval") Boolean requiresApproval,
            @Param("requiresDocuments") Boolean requiresDocuments,
            @Param("carryForward") Boolean carryForward,
            @Param("appliesToProbation") Boolean appliesToProbation,
            @Param("active") Boolean active);

    /**
     * Get leave types count by status
     */
    @Query("SELECT " +
           "SUM(CASE WHEN lt.active = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN lt.active = false THEN 1 ELSE 0 END) as inactiveCount, " +
           "COUNT(lt) as totalCount " +
           "FROM LeaveType lt")
    Object[] getLeaveTypeStats();

    /**
     * Find leave types by minimum notice days
     */
    List<LeaveType> findByMinimumNoticeDaysLessThanEqualAndActiveTrue(Integer noticeDays);

    /**
     * Find leave types by maximum consecutive days
     */
    List<LeaveType> findByMaximumConsecutiveDaysGreaterThanEqualAndActiveTrue(Integer consecutiveDays);

    /**
     * Get next display order
     */
    @Query("SELECT COALESCE(MAX(lt.displayOrder), 0) + 1 FROM LeaveType lt")
    Integer getNextDisplayOrder();

    /**
     * Update display orders
     */
    @Query("UPDATE LeaveType lt SET lt.displayOrder = lt.displayOrder + 1 " +
           "WHERE lt.displayOrder >= :fromOrder")
    void incrementDisplayOrdersFrom(@Param("fromOrder") Integer fromOrder);
}