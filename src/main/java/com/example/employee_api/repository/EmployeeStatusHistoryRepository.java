package com.example.employee_api.repository;

import com.example.employee_api.model.Employee;
import com.example.employee_api.model.EmployeeStatusHistory;
import com.example.employee_api.model.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EmployeeStatusHistory entity
 */
@Repository
public interface EmployeeStatusHistoryRepository extends JpaRepository<EmployeeStatusHistory, Long> {

    /**
     * Find all status history records for a specific employee
     */
    List<EmployeeStatusHistory> findByEmployeeOrderByChangedAtDesc(Employee employee);

    /**
     * Find status history for an employee with pagination
     */
    Page<EmployeeStatusHistory> findByEmployeeOrderByChangedAtDesc(Employee employee, Pageable pageable);

    /**
     * Find status history by employee ID
     */
    @Query("SELECT sh FROM EmployeeStatusHistory sh WHERE sh.employee.id = :employeeId ORDER BY sh.changedAt DESC")
    List<EmployeeStatusHistory> findByEmployeeIdOrderByChangedAtDesc(@Param("employeeId") Long employeeId);

    /**
     * Find status history by employee ID with pagination
     */
    @Query("SELECT sh FROM EmployeeStatusHistory sh WHERE sh.employee.id = :employeeId ORDER BY sh.changedAt DESC")
    Page<EmployeeStatusHistory> findByEmployeeIdOrderByChangedAtDesc(@Param("employeeId") Long employeeId, Pageable pageable);

    /**
     * Find the most recent status change for an employee
     */
    Optional<EmployeeStatusHistory> findFirstByEmployeeOrderByChangedAtDesc(Employee employee);

    /**
     * Find status changes by new status
     */
    List<EmployeeStatusHistory> findByNewStatusOrderByChangedAtDesc(EmployeeStatus newStatus);

    /**
     * Find status changes between dates
     */
    @Query("SELECT sh FROM EmployeeStatusHistory sh WHERE sh.changedAt BETWEEN :startDate AND :endDate ORDER BY sh.changedAt DESC")
    List<EmployeeStatusHistory> findByChangedAtBetweenOrderByChangedAtDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find status changes by who changed them
     */
    List<EmployeeStatusHistory> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * Count status changes for an employee
     */
    long countByEmployee(Employee employee);

    /**
     * Count status changes by status
     */
    long countByNewStatus(EmployeeStatus newStatus);

    /**
     * Find employees who were activated in a date range
     */
    @Query("SELECT DISTINCT sh.employee FROM EmployeeStatusHistory sh WHERE sh.newStatus = 'ACTIVE' AND sh.changedAt BETWEEN :startDate AND :endDate")
    List<Employee> findEmployeesActivatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find employees who were terminated in a date range
     */
    @Query("SELECT DISTINCT sh.employee FROM EmployeeStatusHistory sh WHERE sh.newStatus = 'TERMINATED' AND sh.changedAt BETWEEN :startDate AND :endDate")
    List<Employee> findEmployeesTerminatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}