package com.example.employee_api.repository;

import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.model.enums.EmploymentType;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    
    // Basic query methods
    List<Employee> findByJobTitle(String jobTitle);
    List<Employee> findByStatus(EmployeeStatus status);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeId(String employeeId);
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    
    // Advanced search methods
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Employee> findByFullNameContaining(@Param("name") String name);
    
    // Department-based queries
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId")
    Page<Employee> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.department.name = :departmentName")
    List<Employee> findByDepartmentName(@Param("departmentName") String departmentName);
    
    // Position-based queries
    @Query("SELECT e FROM Employee e WHERE e.currentPosition.id = :positionId")
    List<Employee> findByCurrentPositionId(@Param("positionId") Long positionId);
    
    @Query("SELECT e FROM Employee e WHERE e.currentPosition.title = :positionTitle")
    List<Employee> findByCurrentPositionTitle(@Param("positionTitle") String positionTitle);
    
    // Date range queries
    List<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate);
    List<Employee> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT e FROM Employee e WHERE e.hireDate >= :date")
    List<Employee> findHiredAfter(@Param("date") LocalDate date);
    
    @Query("SELECT e FROM Employee e WHERE e.hireDate <= :date")
    List<Employee> findHiredBefore(@Param("date") LocalDate date);
    
    // Salary-based queries
    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :minSalary AND :maxSalary")
    List<Employee> findBySalaryRange(@Param("minSalary") BigDecimal minSalary, @Param("maxSalary") BigDecimal maxSalary);
    
    @Query("SELECT e FROM Employee e WHERE e.salary >= :salary")
    List<Employee> findBySalaryGreaterThanEqual(@Param("salary") BigDecimal salary);
    
    @Query("SELECT e FROM Employee e WHERE e.salary <= :salary")
    List<Employee> findBySalaryLessThanEqual(@Param("salary") BigDecimal salary);
    
    // Employment type queries
    List<Employee> findByEmploymentType(EmploymentType employmentType);
    
    @Query("SELECT e FROM Employee e WHERE e.employmentType IN :types")
    List<Employee> findByEmploymentTypes(@Param("types") List<EmploymentType> types);
    
    // Manager-based queries
    @Query("SELECT e FROM Employee e WHERE e.managerId = :managerId")
    List<Employee> findByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT e FROM Employee e WHERE e.managerId IS NULL")
    List<Employee> findEmployeesWithoutManager();
    
    // Location-based queries
    @Query("SELECT e FROM Employee e WHERE LOWER(e.address) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Employee> findByLocationContaining(@Param("location") String location);
    
    @Query("SELECT e FROM Employee e WHERE LOWER(e.city) = LOWER(:city)")
    List<Employee> findByCity(@Param("city") String city);
    
    @Query("SELECT e FROM Employee e WHERE LOWER(e.state) = LOWER(:state)")
    List<Employee> findByState(@Param("state") String state);
    
    @Query("SELECT e FROM Employee e WHERE e.postalCode = :postalCode")
    List<Employee> findByPostalCode(@Param("postalCode") String postalCode);
    
    // Advanced analytics queries
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
    Long countByStatus(@Param("status") EmployeeStatus status);
    
    @Query("SELECT e.department.name, COUNT(e) FROM Employee e GROUP BY e.department.name")
    List<Object[]> countEmployeesByDepartment();
    
    @Query("SELECT e.employmentType, COUNT(e) FROM Employee e GROUP BY e.employmentType")
    List<Object[]> countEmployeesByEmploymentType();
    
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department.id = :departmentId")
    BigDecimal getAverageSalaryByDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT MIN(e.salary), MAX(e.salary), AVG(e.salary) FROM Employee e WHERE e.currentPosition.id = :positionId")
    List<Object[]> getSalaryStatsByPosition(@Param("positionId") Long positionId);
    
    // Recent activities
    @Query("SELECT e FROM Employee e WHERE e.createdAt >= :date ORDER BY e.createdAt DESC")
    List<Employee> findRecentlyCreated(@Param("date") LocalDate date);
    
    @Query("SELECT e FROM Employee e WHERE e.updatedAt >= :date ORDER BY e.updatedAt DESC")
    List<Employee> findRecentlyUpdated(@Param("date") LocalDate date);
    
    // Search with multiple criteria
    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:employmentType IS NULL OR e.employmentType = :employmentType)")
    Page<Employee> searchEmployees(@Param("name") String name,
                                  @Param("email") String email,
                                  @Param("departmentId") Long departmentId,
                                  @Param("status") EmployeeStatus status,
                                  @Param("employmentType") EmploymentType employmentType,
                                  Pageable pageable);
    
    // Full-text search simulation (for databases without full-text search)
    @Query("SELECT DISTINCT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Employee> fullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
}