package com.example.employee_api.controller;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.*;
import com.example.employee_api.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing payroll operations including pay grades,
 * salary history, bonuses, and deductions.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PayrollController {

    private final PayrollService payrollService;

    @Autowired
    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ==================== SALARY & COMPENSATION ENDPOINTS ====================

    /**
     * GET /api/payroll/salaries - Get salary information
     */
    @GetMapping("/payroll/salaries")
    public ResponseEntity<Map<String, Object>> getSalaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PayGrade> payGrades = payrollService.getPayGrades(PayGradeStatus.ACTIVE, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("payGrades", payGrades.getContent());
        response.put("currentPage", payGrades.getNumber());
        response.put("totalItems", payGrades.getTotalElements());
        response.put("totalPages", payGrades.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/employees/{id}/salary - Get employee salary details
     */
    @GetMapping("/employees/{id}/salary")
    public ResponseEntity<Map<String, Object>> getEmployeeSalary(@PathVariable Long id) {
        BigDecimal currentSalary = payrollService.getCurrentSalaryForEmployee(id);
        List<SalaryHistory> salaryHistory = payrollService.getSalaryHistoryForEmployee(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("currentSalary", currentSalary);
        response.put("salaryHistory", salaryHistory);
        
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/employees/{id}/salary - Update employee salary
     */
    @PutMapping("/employees/{id}/salary")
    public ResponseEntity<SalaryHistory> updateEmployeeSalary(
            @PathVariable Long id,
            @Valid @RequestBody SalaryHistory salaryHistory) {
        
        SalaryHistory updatedSalary = payrollService.recordSalaryChange(id, salaryHistory);
        return ResponseEntity.ok(updatedSalary);
    }

    /**
     * GET /api/employees/{id}/salary-history - Get salary history
     */
    @GetMapping("/employees/{id}/salary-history")
    public ResponseEntity<List<SalaryHistory>> getEmployeeSalaryHistory(@PathVariable Long id) {
        List<SalaryHistory> salaryHistory = payrollService.getSalaryHistoryForEmployee(id);
        return ResponseEntity.ok(salaryHistory);
    }

    /**
     * POST /api/payroll/salary-adjustment - Process salary adjustment
     */
    @PostMapping("/payroll/salary-adjustment")
    public ResponseEntity<SalaryHistory> processSalaryAdjustment(
            @RequestParam Long employeeId,
            @Valid @RequestBody SalaryHistory salaryAdjustment) {
        
        SalaryHistory processedAdjustment = payrollService.recordSalaryChange(employeeId, salaryAdjustment);
        return ResponseEntity.status(HttpStatus.CREATED).body(processedAdjustment);
    }

    /**
     * GET /api/payroll/pay-grades - Get pay grade structure
     */
    @GetMapping("/payroll/pay-grades")
    public ResponseEntity<List<PayGrade>> getPayGrades(
            @RequestParam(required = false) PayGradeStatus status,
            @RequestParam(required = false) String search) {
        
        List<PayGrade> payGrades;
        
        if (search != null && !search.isEmpty()) {
            payGrades = payrollService.searchPayGrades(search);
        } else if (status != null) {
            payGrades = payrollService.getActivePayGrades();
        } else {
            payGrades = payrollService.getAllPayGrades();
        }
        
        return ResponseEntity.ok(payGrades);
    }

    /**
     * PUT /api/payroll/pay-grades - Update pay grades
     */
    @PutMapping("/payroll/pay-grades")
    public ResponseEntity<PayGrade> updatePayGrade(
            @RequestParam Long id,
            @Valid @RequestBody PayGrade payGrade) {
        
        PayGrade updatedPayGrade = payrollService.updatePayGrade(id, payGrade);
        return ResponseEntity.ok(updatedPayGrade);
    }

    // ==================== BONUSES & DEDUCTIONS ENDPOINTS ====================

    /**
     * GET /api/payroll/bonuses - Get bonus records
     */
    @GetMapping("/payroll/bonuses")
    public ResponseEntity<List<Bonus>> getBonuses(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) BonusStatus status,
            @RequestParam(required = false) BonusType type) {
        
        List<Bonus> bonuses;
        
        if (employeeId != null) {
            bonuses = payrollService.getBonusesForEmployee(employeeId);
        } else if (status != null) {
            bonuses = payrollService.getBonusesByStatus(status);
        } else {
            // Return all bonuses - in a real app, you'd want pagination here
            bonuses = payrollService.getBonusesByStatus(BonusStatus.PENDING);
        }
        
        return ResponseEntity.ok(bonuses);
    }

    /**
     * POST /api/payroll/bonuses - Create bonus record
     */
    @PostMapping("/payroll/bonuses")
    public ResponseEntity<Bonus> createBonus(
            @RequestParam Long employeeId,
            @Valid @RequestBody Bonus bonus) {
        
        Bonus createdBonus = payrollService.createBonus(employeeId, bonus);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBonus);
    }

    /**
     * GET /api/payroll/deductions - Get deduction records
     */
    @GetMapping("/payroll/deductions")
    public ResponseEntity<List<Deduction>> getDeductions(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) DeductionStatus status,
            @RequestParam(required = false) DeductionType type) {
        
        List<Deduction> deductions;
        
        if (employeeId != null) {
            if (status != null && status == DeductionStatus.ACTIVE) {
                deductions = payrollService.getActiveDeductionsForEmployee(employeeId);
            } else {
                deductions = payrollService.getDeductionsForEmployee(employeeId);
            }
        } else if (status != null) {
            deductions = payrollService.getDeductionsByStatus(status);
        } else {
            // Return active deductions by default
            deductions = payrollService.getDeductionsByStatus(DeductionStatus.ACTIVE);
        }
        
        return ResponseEntity.ok(deductions);
    }

    /**
     * POST /api/payroll/deductions - Create deduction record
     */
    @PostMapping("/payroll/deductions")
    public ResponseEntity<Deduction> createDeduction(
            @RequestParam Long employeeId,
            @Valid @RequestBody Deduction deduction) {
        
        Deduction createdDeduction = payrollService.createDeduction(employeeId, deduction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDeduction);
    }

    /**
     * GET /api/employees/{id}/compensation-summary - Get total compensation
     */
    @GetMapping("/employees/{id}/compensation-summary")
    public ResponseEntity<Map<String, Object>> getCompensationSummary(@PathVariable Long id) {
        BigDecimal currentSalary = payrollService.getCurrentSalaryForEmployee(id);
        BigDecimal totalBonuses = payrollService.getTotalBonusAmountForEmployee(id);
        List<Deduction> activeDeductions = payrollService.getActiveDeductionsForEmployee(id);
        BigDecimal totalDeductions = payrollService.calculateTotalDeductionsForEmployee(id, currentSalary);
        
        BigDecimal grossCompensation = currentSalary.add(totalBonuses);
        BigDecimal netCompensation = grossCompensation.subtract(totalDeductions);
        
        Map<String, Object> compensationSummary = new HashMap<>();
        compensationSummary.put("currentSalary", currentSalary);
        compensationSummary.put("totalBonuses", totalBonuses);
        compensationSummary.put("totalDeductions", totalDeductions);
        compensationSummary.put("grossCompensation", grossCompensation);
        compensationSummary.put("netCompensation", netCompensation);
        compensationSummary.put("activeDeductions", activeDeductions);
        
        return ResponseEntity.ok(compensationSummary);
    }

    // ==================== ADDITIONAL PAYROLL ENDPOINTS ====================

    /**
     * POST /api/payroll/pay-grades - Create pay grade
     */
    @PostMapping("/payroll/pay-grades")
    public ResponseEntity<PayGrade> createPayGrade(@Valid @RequestBody PayGrade payGrade) {
        PayGrade createdPayGrade = payrollService.createPayGrade(payGrade);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayGrade);
    }

    /**
     * GET /api/payroll/pay-grades/{id} - Get pay grade by ID
     */
    @GetMapping("/payroll/pay-grades/{id}")
    public ResponseEntity<PayGrade> getPayGradeById(@PathVariable Long id) {
        PayGrade payGrade = payrollService.getPayGradeById(id);
        return ResponseEntity.ok(payGrade);
    }

    /**
     * DELETE /api/payroll/pay-grades/{id} - Delete pay grade
     */
    @DeleteMapping("/payroll/pay-grades/{id}")
    public ResponseEntity<Void> deletePayGrade(@PathVariable Long id) {
        payrollService.deletePayGrade(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/payroll/bonuses/{id} - Update bonus
     */
    @PutMapping("/payroll/bonuses/{id}")
    public ResponseEntity<Bonus> updateBonus(
            @PathVariable Long id,
            @Valid @RequestBody Bonus bonus) {
        
        Bonus updatedBonus = payrollService.updateBonus(id, bonus);
        return ResponseEntity.ok(updatedBonus);
    }

    /**
     * GET /api/payroll/bonuses/{id} - Get bonus by ID
     */
    @GetMapping("/payroll/bonuses/{id}")
    public ResponseEntity<Bonus> getBonusById(@PathVariable Long id) {
        Bonus bonus = payrollService.getBonusById(id);
        return ResponseEntity.ok(bonus);
    }

    /**
     * DELETE /api/payroll/bonuses/{id} - Delete bonus
     */
    @DeleteMapping("/payroll/bonuses/{id}")
    public ResponseEntity<Void> deleteBonus(@PathVariable Long id) {
        payrollService.deleteBonus(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/payroll/bonuses/{id}/approve - Approve bonus
     */
    @PutMapping("/payroll/bonuses/{id}/approve")
    public ResponseEntity<Bonus> approveBonus(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        
        Bonus approvedBonus = payrollService.approveBonus(id, approvedBy);
        return ResponseEntity.ok(approvedBonus);
    }

    /**
     * PUT /api/payroll/bonuses/{id}/pay - Mark bonus as paid
     */
    @PutMapping("/payroll/bonuses/{id}/pay")
    public ResponseEntity<Bonus> markBonusAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate paymentDate) {
        
        LocalDate dateToUse = paymentDate != null ? paymentDate : LocalDate.now();
        Bonus paidBonus = payrollService.markBonusAsPaid(id, dateToUse);
        return ResponseEntity.ok(paidBonus);
    }

    /**
     * PUT /api/payroll/deductions/{id} - Update deduction
     */
    @PutMapping("/payroll/deductions/{id}")
    public ResponseEntity<Deduction> updateDeduction(
            @PathVariable Long id,
            @Valid @RequestBody Deduction deduction) {
        
        Deduction updatedDeduction = payrollService.updateDeduction(id, deduction);
        return ResponseEntity.ok(updatedDeduction);
    }

    /**
     * GET /api/payroll/deductions/{id} - Get deduction by ID
     */
    @GetMapping("/payroll/deductions/{id}")
    public ResponseEntity<Deduction> getDeductionById(@PathVariable Long id) {
        Deduction deduction = payrollService.getDeductionById(id);
        return ResponseEntity.ok(deduction);
    }

    /**
     * DELETE /api/payroll/deductions/{id} - Delete deduction
     */
    @DeleteMapping("/payroll/deductions/{id}")
    public ResponseEntity<Void> deleteDeduction(@PathVariable Long id) {
        payrollService.deleteDeduction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/payroll/pay-grades/suitable - Find suitable pay grades for salary
     */
    @GetMapping("/payroll/pay-grades/suitable")
    public ResponseEntity<List<PayGrade>> getSuitablePayGrades(@RequestParam BigDecimal salary) {
        List<PayGrade> suitablePayGrades = payrollService.findSuitablePayGradesForSalary(salary);
        return ResponseEntity.ok(suitablePayGrades);
    }
}