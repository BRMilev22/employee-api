package com.example.employee_api.repository;

import com.example.employee_api.model.Department;
import com.example.employee_api.model.enums.DepartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    // Find by department code
    Optional<Department> findByDepartmentCode(String departmentCode);
    
    // Find by name
    List<Department> findByNameContainingIgnoreCase(String name);
    
    // Find by status
    List<Department> findByStatus(DepartmentStatus status);
    
    // Find by location
    List<Department> findByLocationContainingIgnoreCase(String location);
    
    // Check if department code exists
    boolean existsByDepartmentCode(String departmentCode);
    
    // Find root departments (no parent)
    List<Department> findByParentDepartmentIsNull();
    
    // Find sub-departments by parent
    List<Department> findByParentDepartment(Department parentDepartment);
    
    // Find departments by manager
    List<Department> findByManager_Id(Long managerId);
    
    // Custom queries
    @Query("SELECT d FROM Department d WHERE d.parentDepartment.id = :parentId")
    List<Department> findSubDepartmentsByParentId(@Param("parentId") Long parentId);
    
    @Query("SELECT d FROM Department d WHERE d.status = :status AND d.parentDepartment IS NULL")
    List<Department> findRootDepartmentsByStatus(@Param("status") DepartmentStatus status);
    
    @Query("SELECT d FROM Department d JOIN d.employees e WHERE e.id = :employeeId")
    Optional<Department> findByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    Long countEmployeesByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT d FROM Department d WHERE d.budget >= :minBudget AND d.budget <= :maxBudget")
    List<Department> findByBudgetBetween(@Param("minBudget") java.math.BigDecimal minBudget, 
                                        @Param("maxBudget") java.math.BigDecimal maxBudget);
    
    // Find departments with no manager assigned
    @Query("SELECT d FROM Department d WHERE d.manager IS NULL AND d.status = 'ACTIVE'")
    List<Department> findActiveDepartmentsWithoutManager();
    
    // Get department hierarchy (recursive)
    @Query(value = "WITH RECURSIVE dept_hierarchy AS (" +
           "  SELECT id, name, department_code, parent_department_id, 0 as level " +
           "  FROM departments WHERE parent_department_id IS NULL " +
           "  UNION ALL " +
           "  SELECT d.id, d.name, d.department_code, d.parent_department_id, dh.level + 1 " +
           "  FROM departments d " +
           "  INNER JOIN dept_hierarchy dh ON d.parent_department_id = dh.id" +
           ") SELECT * FROM dept_hierarchy ORDER BY level, name", nativeQuery = true)
    List<Object[]> getDepartmentHierarchy();
}