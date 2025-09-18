package com.example.employee_api.controller;

import com.example.employee_api.model.EmployeePositionHistory;
import com.example.employee_api.model.Position;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import com.example.employee_api.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for managing positions and job titles
 * Provides endpoints for position CRUD operations and employee assignments
 */
@RestController
@RequestMapping("/api/positions")
@CrossOrigin(origins = "*")
public class PositionController {

    private final PositionService positionService;

    @Autowired
    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    /**
     * Create a new position
     */
    @PostMapping
    public ResponseEntity<Position> createPosition(@Valid @RequestBody Position position) {
        Position createdPosition = positionService.createPosition(position);
        return new ResponseEntity<>(createdPosition, HttpStatus.CREATED);
    }

    /**
     * Get all positions
     */
    @GetMapping
    public ResponseEntity<List<Position>> getAllPositions() {
        List<Position> positions = positionService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * Get position by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Position> getPositionById(@PathVariable Long id) {
        Position position = positionService.getPositionById(id);
        return ResponseEntity.ok(position);
    }

    /**
     * Update position
     */
    @PutMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable Long id, 
                                                  @Valid @RequestBody Position position) {
        Position updatedPosition = positionService.updatePosition(id, position);
        return ResponseEntity.ok(updatedPosition);
    }

    /**
     * Delete position (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get active positions
     */
    @GetMapping("/active")
    public ResponseEntity<List<Position>> getActivePositions() {
        List<Position> positions = positionService.getActivePositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * Get available positions (with openings)
     */
    @GetMapping("/available")
    public ResponseEntity<List<Position>> getAvailablePositions() {
        List<Position> positions = positionService.getAvailablePositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * Get positions by department
     */
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Position>> getPositionsByDepartment(@PathVariable Long departmentId) {
        List<Position> positions = positionService.getPositionsByDepartment(departmentId);
        return ResponseEntity.ok(positions);
    }

    /**
     * Get positions by level
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<List<Position>> getPositionsByLevel(@PathVariable PositionLevel level) {
        List<Position> positions = positionService.getPositionsByLevel(level);
        return ResponseEntity.ok(positions);
    }

    /**
     * Search positions by title
     */
    @GetMapping("/search")
    public ResponseEntity<List<Position>> searchPositions(@RequestParam String title) {
        List<Position> positions = positionService.searchPositionsByTitle(title);
        return ResponseEntity.ok(positions);
    }

    /**
     * Get positions by salary range
     */
    @GetMapping("/salary-range")
    public ResponseEntity<List<Position>> getPositionsBySalaryRange(
            @RequestParam BigDecimal minSalary,
            @RequestParam BigDecimal maxSalary) {
        List<Position> positions = positionService.getPositionsBySalaryRange(minSalary, maxSalary);
        return ResponseEntity.ok(positions);
    }

    /**
     * Get entry-level positions for a department
     */
    @GetMapping("/department/{departmentId}/entry-level")
    public ResponseEntity<List<Position>> getEntryLevelPositions(@PathVariable Long departmentId) {
        List<Position> positions = positionService.getEntryLevelPositions(departmentId);
        return ResponseEntity.ok(positions);
    }

    /**
     * Get management positions
     */
    @GetMapping("/management")
    public ResponseEntity<List<Position>> getManagementPositions() {
        List<Position> positions = positionService.getManagementPositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * Assign employee to position
     */
    @PostMapping("/{positionId}/assign-employee")
    public ResponseEntity<EmployeePositionHistory> assignEmployeeToPosition(
            @PathVariable Long positionId,
            @RequestParam Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam BigDecimal salary,
            @RequestParam(required = false) String reason) {
        
        EmployeePositionHistory assignment = positionService.assignEmployeeToPosition(
                employeeId, positionId, startDate, salary, reason);
        return new ResponseEntity<>(assignment, HttpStatus.CREATED);
    }

    /**
     * End employee position assignment
     */
    @PostMapping("/end-assignment")
    public ResponseEntity<Void> endEmployeePositionAssignment(
            @RequestParam Long employeeId,
            @RequestParam LocalDate endDate,
            @RequestParam String reason) {
        
        positionService.endEmployeePositionAssignment(employeeId, endDate, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Get employee position history
     */
    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<List<EmployeePositionHistory>> getEmployeePositionHistory(
            @PathVariable Long employeeId) {
        List<EmployeePositionHistory> history = positionService.getEmployeePositionHistory(employeeId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get position assignment history
     */
    @GetMapping("/{positionId}/history")
    public ResponseEntity<List<EmployeePositionHistory>> getPositionAssignmentHistory(
            @PathVariable Long positionId) {
        List<EmployeePositionHistory> history = positionService.getPositionAssignmentHistory(positionId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get current employees in position
     */
    @GetMapping("/{positionId}/current-employees")
    public ResponseEntity<List<EmployeePositionHistory>> getCurrentEmployeesInPosition(
            @PathVariable Long positionId) {
        List<EmployeePositionHistory> currentEmployees = 
                positionService.getCurrentEmployeesInPosition(positionId);
        return ResponseEntity.ok(currentEmployees);
    }

    /**
     * Get position statistics
     */
    @GetMapping("/{positionId}/statistics")
    public ResponseEntity<PositionService.PositionStatistics> getPositionStatistics(
            @PathVariable Long positionId) {
        PositionService.PositionStatistics stats = positionService.getPositionStatistics(positionId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all position levels
     */
    @GetMapping("/levels")
    public ResponseEntity<PositionLevel[]> getPositionLevels() {
        return ResponseEntity.ok(PositionLevel.values());
    }

    /**
     * Get all position statuses
     */
    @GetMapping("/statuses")
    public ResponseEntity<PositionStatus[]> getPositionStatuses() {
        return ResponseEntity.ok(PositionStatus.values());
    }
}