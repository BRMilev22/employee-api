package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.model.Employee;
import com.example.employee_api.model.EmployeePositionHistory;
import com.example.employee_api.model.Position;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import com.example.employee_api.repository.EmployeePositionHistoryRepository;
import com.example.employee_api.repository.EmployeeRepository;
import com.example.employee_api.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing positions and job titles
 * Handles business logic for position management and employee position assignments
 */
@Service
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;
    private final EmployeePositionHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PositionService(PositionRepository positionRepository, 
                          EmployeePositionHistoryRepository historyRepository,
                          EmployeeRepository employeeRepository) {
        this.positionRepository = positionRepository;
        this.historyRepository = historyRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Create a new position
     */
    public Position createPosition(Position position) {
        validatePosition(position);
        return positionRepository.save(position);
    }

    /**
     * Update an existing position
     */
    public Position updatePosition(Long id, Position updatedPosition) {
        Position existingPosition = getPositionById(id);
        
        // Update fields
        existingPosition.setTitle(updatedPosition.getTitle());
        existingPosition.setDescription(updatedPosition.getDescription());
        existingPosition.setLevel(updatedPosition.getLevel());
        existingPosition.setStatus(updatedPosition.getStatus());
        existingPosition.setDepartment(updatedPosition.getDepartment());
        existingPosition.setMinSalary(updatedPosition.getMinSalary());
        existingPosition.setMaxSalary(updatedPosition.getMaxSalary());
        existingPosition.setPayGrade(updatedPosition.getPayGrade());
        existingPosition.setRequiredQualifications(updatedPosition.getRequiredQualifications());
        existingPosition.setPreferredQualifications(updatedPosition.getPreferredQualifications());
        existingPosition.setRequiredSkills(updatedPosition.getRequiredSkills());
        existingPosition.setPreferredSkills(updatedPosition.getPreferredSkills());
        existingPosition.setMinExperienceYears(updatedPosition.getMinExperienceYears());
        existingPosition.setMaxExperienceYears(updatedPosition.getMaxExperienceYears());
        existingPosition.setReportsTo(updatedPosition.getReportsTo());
        existingPosition.setNumberOfOpenings(updatedPosition.getNumberOfOpenings());
        existingPosition.setTotalHeadcount(updatedPosition.getTotalHeadcount());
        
        validatePosition(existingPosition);
        return positionRepository.save(existingPosition);
    }

    /**
     * Get position by ID
     */
    @Transactional(readOnly = true)
    public Position getPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Position not found with id: " + id));
    }

    /**
     * Get all positions
     */
    @Transactional(readOnly = true)
    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    /**
     * Get active positions
     */
    @Transactional(readOnly = true)
    public List<Position> getActivePositions() {
        return positionRepository.findByStatusOrderByTitleAsc(PositionStatus.ACTIVE);
    }

    /**
     * Get positions with openings
     */
    @Transactional(readOnly = true)
    public List<Position> getAvailablePositions() {
        return positionRepository.findAvailablePositions();
    }

    /**
     * Get positions by department
     */
    @Transactional(readOnly = true)
    public List<Position> getPositionsByDepartment(Long departmentId) {
        return positionRepository.findByDepartmentId(departmentId);
    }

    /**
     * Get positions by level
     */
    @Transactional(readOnly = true)
    public List<Position> getPositionsByLevel(PositionLevel level) {
        return positionRepository.findByLevel(level);
    }

    /**
     * Search positions by title
     */
    @Transactional(readOnly = true)
    public List<Position> searchPositionsByTitle(String title) {
        return positionRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Get positions by salary range
     */
    @Transactional(readOnly = true)
    public List<Position> getPositionsBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        return positionRepository.findPositionsWithOverlappingSalaryRange(minSalary, maxSalary);
    }

    /**
     * Get entry-level positions for a department
     */
    @Transactional(readOnly = true)
    public List<Position> getEntryLevelPositions(Long departmentId) {
        return positionRepository.findEntryLevelPositionsByDepartment(departmentId);
    }

    /**
     * Get management positions
     */
    @Transactional(readOnly = true)
    public List<Position> getManagementPositions() {
        return positionRepository.findManagementPositions();
    }

    /**
     * Assign employee to position
     */
    public EmployeePositionHistory assignEmployeeToPosition(Long employeeId, Long positionId, 
                                                           LocalDate startDate, BigDecimal salary, 
                                                           String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        
        Position position = getPositionById(positionId);
        
        // Check if position has openings
        if (!position.canAcceptNewEmployees()) {
            throw new IllegalStateException("Position has no available openings");
        }
        
        // End current position if exists
        Optional<EmployeePositionHistory> currentPosition = 
                historyRepository.findByEmployeeIdAndIsCurrentTrue(employeeId);
        
        if (currentPosition.isPresent()) {
            EmployeePositionHistory current = currentPosition.get();
            current.endAssignment(startDate.minusDays(1), "Position change");
            current.setEndingSalary(employee.getSalary());
            historyRepository.save(current);
        }
        
        // Create new position assignment
        EmployeePositionHistory newAssignment = new EmployeePositionHistory(employee, position, startDate, salary);
        newAssignment.setChangeReason(reason);
        newAssignment.setDepartmentAtTime(position.getDepartment() != null ? 
                                         position.getDepartment().getName() : null);
        
        // Update employee current position and salary
        employee.setCurrentPosition(position);
        employee.setSalary(salary);
        employeeRepository.save(employee);
        
        // Update position openings
        if (position.getNumberOfOpenings() != null && position.getNumberOfOpenings() > 0) {
            position.setNumberOfOpenings(position.getNumberOfOpenings() - 1);
            positionRepository.save(position);
        }
        
        return historyRepository.save(newAssignment);
    }

    /**
     * End employee position assignment
     */
    public void endEmployeePositionAssignment(Long employeeId, LocalDate endDate, String reason) {
        EmployeePositionHistory currentAssignment = historyRepository
                .findByEmployeeIdAndIsCurrentTrue(employeeId)
                .orElseThrow(() -> new IllegalStateException("No current position found for employee"));
        
        Employee employee = currentAssignment.getEmployee();
        Position position = currentAssignment.getPosition();
        
        // End the assignment
        currentAssignment.endAssignment(endDate, reason);
        currentAssignment.setEndingSalary(employee.getSalary());
        historyRepository.save(currentAssignment);
        
        // Clear employee current position
        employee.setCurrentPosition(null);
        employeeRepository.save(employee);
        
        // Update position openings
        if (position.getNumberOfOpenings() != null) {
            position.setNumberOfOpenings(position.getNumberOfOpenings() + 1);
            positionRepository.save(position);
        }
    }

    /**
     * Get employee position history
     */
    @Transactional(readOnly = true)
    public List<EmployeePositionHistory> getEmployeePositionHistory(Long employeeId) {
        return historyRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
    }

    /**
     * Get position assignment history
     */
    @Transactional(readOnly = true)
    public List<EmployeePositionHistory> getPositionAssignmentHistory(Long positionId) {
        return historyRepository.findByPositionIdOrderByStartDateDesc(positionId);
    }

    /**
     * Get current employees in position
     */
    @Transactional(readOnly = true)
    public List<EmployeePositionHistory> getCurrentEmployeesInPosition(Long positionId) {
        return historyRepository.findByPositionIdAndIsCurrentTrue(positionId);
    }

    /**
     * Delete position (soft delete by setting status to INACTIVE)
     */
    public void deletePosition(Long id) {
        Position position = getPositionById(id);
        
        // Check if position has current employees
        List<EmployeePositionHistory> currentEmployees = getCurrentEmployeesInPosition(id);
        if (!currentEmployees.isEmpty()) {
            throw new IllegalStateException("Cannot delete position with current employees. " +
                                          "Please reassign employees first.");
        }
        
        position.setStatus(PositionStatus.INACTIVE);
        positionRepository.save(position);
    }

    /**
     * Get position statistics
     */
    @Transactional(readOnly = true)
    public PositionStatistics getPositionStatistics(Long positionId) {
        Position position = getPositionById(positionId);
        Long totalEmployeeCount = historyRepository.countDistinctEmployeesByPosition(positionId);
        Long currentEmployeeCount = (long) getCurrentEmployeesInPosition(positionId).size();
        Double averageTenure = historyRepository.getAverageTenureForPosition(positionId);
        
        return new PositionStatistics(position, totalEmployeeCount, currentEmployeeCount, averageTenure);
    }

    /**
     * Validate position data
     */
    private void validatePosition(Position position) {
        if (position.getMinSalary() != null && position.getMaxSalary() != null) {
            if (position.getMinSalary().compareTo(position.getMaxSalary()) > 0) {
                throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary");
            }
        }
        
        if (position.getMinExperienceYears() != null && position.getMaxExperienceYears() != null) {
            if (position.getMinExperienceYears() > position.getMaxExperienceYears()) {
                throw new IllegalArgumentException("Minimum experience cannot be greater than maximum experience");
            }
        }
        
        if (position.getTotalHeadcount() != null && position.getNumberOfOpenings() != null) {
            if (position.getNumberOfOpenings() > position.getTotalHeadcount()) {
                throw new IllegalArgumentException("Number of openings cannot exceed total headcount");
            }
        }
        
        // Check for circular reporting relationships
        if (position.getReportsTo() != null && position.getId() != null) {
            if (position.getId().equals(position.getReportsTo().getId())) {
                throw new IllegalArgumentException("Position cannot report to itself");
            }
        }
    }

    /**
     * Inner class for position statistics
     */
    public static class PositionStatistics {
        private final Position position;
        private final Long totalEmployeeCount;
        private final Long currentEmployeeCount;
        private final Double averageTenureDays;

        public PositionStatistics(Position position, Long totalEmployeeCount, 
                                Long currentEmployeeCount, Double averageTenureDays) {
            this.position = position;
            this.totalEmployeeCount = totalEmployeeCount;
            this.currentEmployeeCount = currentEmployeeCount;
            this.averageTenureDays = averageTenureDays;
        }

        // Getters
        public Position getPosition() { return position; }
        public Long getTotalEmployeeCount() { return totalEmployeeCount; }
        public Long getCurrentEmployeeCount() { return currentEmployeeCount; }
        public Double getAverageTenureDays() { return averageTenureDays; }
    }
}