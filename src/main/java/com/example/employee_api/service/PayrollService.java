package com.example.employee_api.service;

import com.example.employee_api.exception.EmployeeNotFoundException;
import com.example.employee_api.exception.ResourceNotFoundException;
import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.*;
import com.example.employee_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing payroll operations including pay grades,
 * salary history, bonuses, and deductions.
 */
@Service
@Transactional
public class PayrollService {

    private final PayGradeRepository payGradeRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final BonusRepository bonusRepository;
    private final DeductionRepository deductionRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public PayrollService(PayGradeRepository payGradeRepository,
                         SalaryHistoryRepository salaryHistoryRepository,
                         BonusRepository bonusRepository,
                         DeductionRepository deductionRepository,
                         EmployeeRepository employeeRepository) {
        this.payGradeRepository = payGradeRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.bonusRepository = bonusRepository;
        this.deductionRepository = deductionRepository;
        this.employeeRepository = employeeRepository;
    }

    // ==================== PAY GRADE OPERATIONS ====================

    /**
     * Create a new pay grade
     */
    public PayGrade createPayGrade(PayGrade payGrade) {
        validatePayGrade(payGrade);
        
        if (payGradeRepository.existsByGradeCode(payGrade.getGradeCode())) {
            throw new IllegalArgumentException("Pay grade with code " + payGrade.getGradeCode() + " already exists");
        }
        
        return payGradeRepository.save(payGrade);
    }

    /**
     * Update an existing pay grade
     */
    public PayGrade updatePayGrade(Long id, PayGrade payGradeDetails) {
        PayGrade payGrade = payGradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pay grade not found with id: " + id));

        validatePayGrade(payGradeDetails);
        
        // Check if grade code is unique (excluding current record)
        if (payGradeRepository.existsByGradeCodeAndIdNot(payGradeDetails.getGradeCode(), id)) {
            throw new IllegalArgumentException("Pay grade with code " + payGradeDetails.getGradeCode() + " already exists");
        }

        payGrade.setGradeCode(payGradeDetails.getGradeCode());
        payGrade.setGradeName(payGradeDetails.getGradeName());
        payGrade.setDescription(payGradeDetails.getDescription());
        payGrade.setMinSalary(payGradeDetails.getMinSalary());
        payGrade.setMaxSalary(payGradeDetails.getMaxSalary());
        payGrade.setGradeLevel(payGradeDetails.getGradeLevel());
        payGrade.setStatus(payGradeDetails.getStatus());

        return payGradeRepository.save(payGrade);
    }

    /**
     * Get all pay grades
     */
    @Transactional(readOnly = true)
    public List<PayGrade> getAllPayGrades() {
        return payGradeRepository.findAll();
    }

    /**
     * Get active pay grades
     */
    @Transactional(readOnly = true)
    public List<PayGrade> getActivePayGrades() {
        return payGradeRepository.findByStatusOrderByGradeLevel(PayGradeStatus.ACTIVE);
    }

    /**
     * Get pay grade by ID
     */
    @Transactional(readOnly = true)
    public PayGrade getPayGradeById(Long id) {
        return payGradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pay grade not found with id: " + id));
    }

    /**
     * Get pay grade by grade code
     */
    @Transactional(readOnly = true)
    public PayGrade getPayGradeByCode(String gradeCode) {
        return payGradeRepository.findByGradeCode(gradeCode)
                .orElseThrow(() -> new IllegalArgumentException("Pay grade not found with code: " + gradeCode));
    }

    /**
     * Delete pay grade
     */
    public void deletePayGrade(Long id) {
        PayGrade payGrade = getPayGradeById(id);
        
        // Check if pay grade is in use by checking the employee repository
        boolean isInUse = employeeRepository.existsByPayGradeId(id);
        if (isInUse) {
            throw new IllegalStateException("Cannot delete pay grade that is assigned to employees");
        }
        
        payGradeRepository.delete(payGrade);
    }

    /**
     * Find suitable pay grades for a salary
     */
    @Transactional(readOnly = true)
    public List<PayGrade> findSuitablePayGradesForSalary(BigDecimal salary) {
        return payGradeRepository.findSuitablePayGradesForSalary(salary);
    }

    // ==================== SALARY HISTORY OPERATIONS ====================

    /**
     * Record salary change
     */
    public SalaryHistory recordSalaryChange(Long employeeId, SalaryHistory salaryHistory) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        salaryHistory.setEmployee(employee);
        
        // Set previous salary from current salary
        if (employee.getSalary() != null) {
            salaryHistory.setPreviousSalary(employee.getSalary());
        }
        
        validateSalaryHistory(salaryHistory);
        
        // Update employee's current salary
        employee.setSalary(salaryHistory.getNewSalary());
        employeeRepository.save(employee);
        
        return salaryHistoryRepository.save(salaryHistory);
    }

    /**
     * Get salary history for employee
     */
    @Transactional(readOnly = true)
    public List<SalaryHistory> getSalaryHistoryForEmployee(Long employeeId) {
        return salaryHistoryRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }

    /**
     * Get current salary for employee
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentSalaryForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));
        return employee.getSalary();
    }

    /**
     * Get salary trend for employee
     */
    @Transactional(readOnly = true)
    public List<Object[]> getSalaryTrendForEmployee(Long employeeId) {
        return salaryHistoryRepository.getSalaryTrendForEmployee(employeeId);
    }

    // ==================== BONUS OPERATIONS ====================

    /**
     * Create bonus
     */
    public Bonus createBonus(Long employeeId, Bonus bonus) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        bonus.setEmployee(employee);
        validateBonus(bonus);
        
        return bonusRepository.save(bonus);
    }

    /**
     * Update bonus
     */
    public Bonus updateBonus(Long id, Bonus bonusDetails) {
        Bonus bonus = bonusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bonus not found with id: " + id));

        validateBonus(bonusDetails);

        bonus.setBonusType(bonusDetails.getBonusType());
        bonus.setAmount(bonusDetails.getAmount());
        bonus.setDescription(bonusDetails.getDescription());
        bonus.setAwardDate(bonusDetails.getAwardDate());
        bonus.setPaymentDate(bonusDetails.getPaymentDate());
        bonus.setStatus(bonusDetails.getStatus());
        bonus.setApprovedBy(bonusDetails.getApprovedBy());
        bonus.setApprovalDate(bonusDetails.getApprovalDate());
        bonus.setPerformancePeriodStart(bonusDetails.getPerformancePeriodStart());
        bonus.setPerformancePeriodEnd(bonusDetails.getPerformancePeriodEnd());
        bonus.setNotes(bonusDetails.getNotes());
        bonus.setTaxWithheld(bonusDetails.getTaxWithheld());

        return bonusRepository.save(bonus);
    }

    /**
     * Get bonuses for employee
     */
    @Transactional(readOnly = true)
    public List<Bonus> getBonusesForEmployee(Long employeeId) {
        return bonusRepository.findByEmployeeIdOrderByAwardDateDesc(employeeId);
    }

    /**
     * Get bonus by ID
     */
    @Transactional(readOnly = true)
    public Bonus getBonusById(Long id) {
        return bonusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus not found with id: " + id));
    }

    /**
     * Approve bonus
     */
    public Bonus approveBonus(Long id, String approvedBy) {
        Bonus bonus = getBonusById(id);
        bonus.approve(approvedBy);
        return bonusRepository.save(bonus);
    }

    /**
     * Mark bonus as paid
     */
    public Bonus markBonusAsPaid(Long id, LocalDate paymentDate) {
        Bonus bonus = getBonusById(id);
        bonus.markAsPaid(paymentDate);
        return bonusRepository.save(bonus);
    }

    /**
     * Delete bonus
     */
    public void deleteBonus(Long id) {
        Bonus bonus = getBonusById(id);
        
        if (bonus.isPaid()) {
            throw new IllegalStateException("Cannot delete a bonus that has been paid");
        }
        
        bonusRepository.delete(bonus);
    }

    /**
     * Get total bonus amount for employee
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBonusAmountForEmployee(Long employeeId) {
        return bonusRepository.getTotalBonusAmountForEmployee(employeeId);
    }

    // ==================== DEDUCTION OPERATIONS ====================

    /**
     * Create deduction
     */
    public Deduction createDeduction(Long employeeId, Deduction deduction) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + employeeId));

        deduction.setEmployee(employee);
        validateDeduction(deduction);
        
        return deductionRepository.save(deduction);
    }

    /**
     * Update deduction
     */
    public Deduction updateDeduction(Long id, Deduction deductionDetails) {
        Deduction deduction = deductionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deduction not found with id: " + id));

        validateDeduction(deductionDetails);

        deduction.setDeductionType(deductionDetails.getDeductionType());
        deduction.setAmount(deductionDetails.getAmount());
        deduction.setPercentage(deductionDetails.getPercentage());
        deduction.setDescription(deductionDetails.getDescription());
        deduction.setEffectiveDate(deductionDetails.getEffectiveDate());
        deduction.setEndDate(deductionDetails.getEndDate());
        deduction.setStatus(deductionDetails.getStatus());
        deduction.setIsPreTax(deductionDetails.getIsPreTax());
        deduction.setIsMandatory(deductionDetails.getIsMandatory());
        deduction.setFrequency(deductionDetails.getFrequency());
        deduction.setEmployerContribution(deductionDetails.getEmployerContribution());
        deduction.setAnnualLimit(deductionDetails.getAnnualLimit());
        deduction.setNotes(deductionDetails.getNotes());
        deduction.setVendorName(deductionDetails.getVendorName());
        deduction.setPolicyNumber(deductionDetails.getPolicyNumber());

        return deductionRepository.save(deduction);
    }

    /**
     * Get deductions for employee
     */
    @Transactional(readOnly = true)
    public List<Deduction> getDeductionsForEmployee(Long employeeId) {
        return deductionRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }

    /**
     * Get active deductions for employee
     */
    @Transactional(readOnly = true)
    public List<Deduction> getActiveDeductionsForEmployee(Long employeeId) {
        return deductionRepository.findActiveDeductionsForEmployee(employeeId);
    }

    /**
     * Get deduction by ID
     */
    @Transactional(readOnly = true)
    public Deduction getDeductionById(Long id) {
        return deductionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with id: " + id));
    }

    /**
     * Delete deduction
     */
    public void deleteDeduction(Long id) {
        Deduction deduction = getDeductionById(id);
        deductionRepository.delete(deduction);
    }

    /**
     * Calculate total deductions for employee
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalDeductionsForEmployee(Long employeeId, BigDecimal grossSalary) {
        List<Deduction> activeDeductions = getActiveDeductionsForEmployee(employeeId);
        BigDecimal totalDeductions = BigDecimal.ZERO;
        
        for (Deduction deduction : activeDeductions) {
            if (!deduction.hasReachedAnnualLimit()) {
                BigDecimal deductionAmount = deduction.calculateDeduction(grossSalary);
                totalDeductions = totalDeductions.add(deductionAmount);
            }
        }
        
        return totalDeductions;
    }

    // ==================== VALIDATION METHODS ====================

    private void validatePayGrade(PayGrade payGrade) {
        if (payGrade.getMinSalary().compareTo(payGrade.getMaxSalary()) > 0) {
            throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary");
        }
        
        if (payGrade.getGradeLevel() < 1) {
            throw new IllegalArgumentException("Grade level must be a positive number");
        }
    }

    private void validateSalaryHistory(SalaryHistory salaryHistory) {
        if (salaryHistory.getEffectiveDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Effective date cannot be in the future");
        }
        
        if (salaryHistory.getNewSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New salary must be greater than zero");
        }
    }

    private void validateBonus(Bonus bonus) {
        if (bonus.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bonus amount must be greater than zero");
        }
        
        if (bonus.getAwardDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Award date cannot be in the future");
        }
        
        if (bonus.getPaymentDate() != null && bonus.getPaymentDate().isBefore(bonus.getAwardDate())) {
            throw new IllegalArgumentException("Payment date cannot be before award date");
        }
        
        if (bonus.getPerformancePeriodStart() != null && bonus.getPerformancePeriodEnd() != null &&
            bonus.getPerformancePeriodStart().isAfter(bonus.getPerformancePeriodEnd())) {
            throw new IllegalArgumentException("Performance period start date cannot be after end date");
        }
    }

    private void validateDeduction(Deduction deduction) {
        if (deduction.getAmount() != null && deduction.getPercentage() != null) {
            throw new IllegalArgumentException("Deduction cannot have both amount and percentage set");
        }
        
        if (deduction.getAmount() == null && deduction.getPercentage() == null) {
            throw new IllegalArgumentException("Deduction must have either amount or percentage set");
        }
        
        if (deduction.getAmount() != null && deduction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deduction amount must be greater than zero");
        }
        
        if (deduction.getPercentage() != null && 
            (deduction.getPercentage().compareTo(BigDecimal.ZERO) <= 0 || 
             deduction.getPercentage().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Deduction percentage must be between 0 and 100");
        }
        
        if (deduction.getEndDate() != null && deduction.getEndDate().isBefore(deduction.getEffectiveDate())) {
            throw new IllegalArgumentException("End date cannot be before effective date");
        }
    }

    // ==================== ADDITIONAL UTILITY METHODS ====================

    /**
     * Get all bonuses by status
     */
    @Transactional(readOnly = true)
    public List<Bonus> getBonusesByStatus(BonusStatus status) {
        return bonusRepository.findByStatusOrderByAwardDateDesc(status);
    }

    /**
     * Get all deductions by status
     */
    @Transactional(readOnly = true)
    public List<Deduction> getDeductionsByStatus(DeductionStatus status) {
        return deductionRepository.findByStatusOrderByEffectiveDateDesc(status);
    }

    /**
     * Search pay grades
     */
    @Transactional(readOnly = true)
    public List<PayGrade> searchPayGrades(String searchTerm) {
        return payGradeRepository.searchPayGrades(searchTerm);
    }

    /**
     * Get pay grades with pagination
     */
    @Transactional(readOnly = true)
    public Page<PayGrade> getPayGrades(PayGradeStatus status, Pageable pageable) {
        return payGradeRepository.findByStatus(status, pageable);
    }
}