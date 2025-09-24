package com.example.employee_api.controller;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.*;
import com.example.employee_api.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PayrollControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayGradeRepository payGradeRepository;

    @Autowired
    private SalaryHistoryRepository salaryHistoryRepository;

    @Autowired
    private BonusRepository bonusRepository;

    @Autowired
    private DeductionRepository deductionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee testEmployee;
    private PayGrade testPayGrade;
    private SalaryHistory testSalaryHistory;
    private Bonus testBonus;
    private Deduction testDeduction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clean up repositories
        salaryHistoryRepository.deleteAll();
        bonusRepository.deleteAll();
        deductionRepository.deleteAll();
        employeeRepository.deleteAll();
        payGradeRepository.deleteAll();

        // Create test pay grade
        testPayGrade = new PayGrade();
        testPayGrade.setGradeCode("G001");
        testPayGrade.setGradeName("Software Engineer I");
        testPayGrade.setTitle("Software Engineer I");  // Add required title field
        testPayGrade.setLevel(1);  // Add required level field
        testPayGrade.setDescription("Entry level software engineer");
        testPayGrade.setMinSalary(new BigDecimal("60000.00"));
        testPayGrade.setMaxSalary(new BigDecimal("80000.00"));
        testPayGrade.setGradeLevel(1);
        testPayGrade.setStatus(PayGradeStatus.ACTIVE);
        testPayGrade = payGradeRepository.save(testPayGrade);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setJobTitle("Software Engineer");
        testEmployee.setGender("MALE");
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setEmploymentType(EmploymentType.FULL_TIME);
        testEmployee.setSalary(new BigDecimal("70000.00"));
        testEmployee.setPayGrade(testPayGrade);
        testEmployee = employeeRepository.save(testEmployee);

        // Create test salary history
        testSalaryHistory = new SalaryHistory();
        testSalaryHistory.setEmployee(testEmployee);
        testSalaryHistory.setPayGrade(testPayGrade);
        testSalaryHistory.setPreviousSalary(new BigDecimal("65000.00"));
        testSalaryHistory.setNewSalary(new BigDecimal("70000.00"));
        testSalaryHistory.setEffectiveDate(LocalDate.now().minusMonths(1));
        testSalaryHistory.setChangeReason(SalaryChangeReason.MERIT_INCREASE);
        testSalaryHistory.setNotes("Annual merit increase");
        testSalaryHistory.setApprovedBy("HR Manager");
        testSalaryHistory.setApprovalDate(LocalDate.now().minusMonths(1));
        testSalaryHistory = salaryHistoryRepository.save(testSalaryHistory);

        // Create test bonus
        testBonus = new Bonus();
        testBonus.setEmployee(testEmployee);
        testBonus.setBonusType(BonusType.PERFORMANCE);
        testBonus.setAmount(new BigDecimal("5000.00"));
        testBonus.setDescription("Q4 Performance Bonus");
        testBonus.setAwardDate(LocalDate.now().minusDays(30));
        testBonus.setStatus(BonusStatus.APPROVED);
        testBonus.setApprovedBy("Manager");
        testBonus.setApprovalDate(LocalDate.now().minusDays(25));
        testBonus = bonusRepository.save(testBonus);

        // Create test deduction
        testDeduction = new Deduction();
        testDeduction.setEmployee(testEmployee);
        testDeduction.setDeductionType(DeductionType.HEALTH_INSURANCE);
        testDeduction.setAmount(new BigDecimal("200.00"));
        testDeduction.setDescription("Monthly health insurance premium");
        testDeduction.setEffectiveDate(LocalDate.now().minusMonths(6));
        testDeduction.setStatus(DeductionStatus.ACTIVE);
        testDeduction.setIsPreTax(true);
        testDeduction.setIsMandatory(false);
        testDeduction.setFrequency("MONTHLY");
        testDeduction = deductionRepository.save(testDeduction);
    }

    // ==================== PAY GRADE TESTS ====================

    @Test
    void getPayGrades_ShouldReturnListOfPayGrades() throws Exception {
        mockMvc.perform(get("/api/payroll/pay-grades"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].gradeCode", is("G001")))
                .andExpect(jsonPath("$[0].gradeName", is("Software Engineer I")))
                .andExpect(jsonPath("$[0].minSalary", is(60000.00)))
                .andExpect(jsonPath("$[0].maxSalary", is(80000.00)))
                .andExpect(jsonPath("$[0].gradeLevel", is(1)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    void getPayGradeById_WhenPayGradeExists_ShouldReturnPayGrade() throws Exception {
        mockMvc.perform(get("/api/payroll/pay-grades/{id}", testPayGrade.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gradeCode", is("G001")))
                .andExpect(jsonPath("$.gradeName", is("Software Engineer I")))
                .andExpect(jsonPath("$.description", is("Entry level software engineer")))
                .andExpect(jsonPath("$.minSalary", is(60000.00)))
                .andExpect(jsonPath("$.maxSalary", is(80000.00)));
    }

    @Test
    void createPayGrade_WithValidData_ShouldCreatePayGrade() throws Exception {
        PayGrade newPayGrade = new PayGrade();
        newPayGrade.setGradeCode("G002");
        newPayGrade.setGradeName("Software Engineer II");
        newPayGrade.setTitle("Software Engineer II");  // Add required title field
        newPayGrade.setLevel(2);  // Add required level field
        newPayGrade.setDescription("Mid-level software engineer");
        newPayGrade.setMinSalary(new BigDecimal("75000.00"));
        newPayGrade.setMaxSalary(new BigDecimal("95000.00"));
        newPayGrade.setGradeLevel(2);
        newPayGrade.setStatus(PayGradeStatus.ACTIVE);

        mockMvc.perform(post("/api/payroll/pay-grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPayGrade)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gradeCode", is("G002")))
                .andExpect(jsonPath("$.gradeName", is("Software Engineer II")))
                .andExpect(jsonPath("$.minSalary", is(75000.00)))
                .andExpect(jsonPath("$.maxSalary", is(95000.00)))
                .andExpect(jsonPath("$.gradeLevel", is(2)));
    }

    @Test
    void updatePayGrade_WithValidData_ShouldUpdatePayGrade() throws Exception {
        PayGrade updatedPayGrade = new PayGrade();
        updatedPayGrade.setGradeCode("G001");
        updatedPayGrade.setGradeName("Software Engineer I - Updated");
        updatedPayGrade.setTitle("Software Engineer I - Updated");  // Add required title field
        updatedPayGrade.setLevel(1);  // Add required level field
        updatedPayGrade.setDescription("Updated entry level software engineer");
        updatedPayGrade.setMinSalary(new BigDecimal("62000.00"));
        updatedPayGrade.setMaxSalary(new BigDecimal("82000.00"));
        updatedPayGrade.setGradeLevel(1);
        updatedPayGrade.setStatus(PayGradeStatus.ACTIVE);

        mockMvc.perform(put("/api/payroll/pay-grades")
                .param("id", testPayGrade.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPayGrade)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gradeName", is("Software Engineer I - Updated")))
                .andExpect(jsonPath("$.description", is("Updated entry level software engineer")))
                .andExpect(jsonPath("$.minSalary", is(62000.00)))
                .andExpect(jsonPath("$.maxSalary", is(82000.00)));
    }

    @Test
    void getSuitablePayGrades_ForSalary_ShouldReturnMatchingPayGrades() throws Exception {
        mockMvc.perform(get("/api/payroll/pay-grades/suitable")
                .param("salary", "70000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].gradeCode", is("G001")));
    }

    // ==================== SALARY TESTS ====================

    @Test
    void getSalaries_ShouldReturnSalaryInformation() throws Exception {
        mockMvc.perform(get("/api/payroll/salaries"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.payGrades", hasSize(1)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(1)));
    }

    @Test
    void getEmployeeSalary_ShouldReturnEmployeeSalaryDetails() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/salary", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentSalary", is(70000.00)))
                .andExpect(jsonPath("$.salaryHistory", hasSize(1)))
                .andExpect(jsonPath("$.salaryHistory[0].newSalary", is(70000.00)))
                .andExpect(jsonPath("$.salaryHistory[0].previousSalary", is(65000.00)));
    }

    @Test
    void getEmployeeSalaryHistory_ShouldReturnSalaryHistory() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/salary-history", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].newSalary", is(70000.00)))
                .andExpect(jsonPath("$[0].previousSalary", is(65000.00)))
                .andExpect(jsonPath("$[0].changeReason", is("MERIT_INCREASE")));
    }

    @Test
    void updateEmployeeSalary_WithValidData_ShouldUpdateSalary() throws Exception {
        SalaryHistory newSalaryHistory = new SalaryHistory();
        newSalaryHistory.setNewSalary(new BigDecimal("75000.00"));
        newSalaryHistory.setEffectiveDate(LocalDate.now());
        newSalaryHistory.setChangeReason(SalaryChangeReason.PROMOTION);
        newSalaryHistory.setNotes("Promotion to Senior Software Engineer");
        newSalaryHistory.setApprovedBy("VP Engineering");
        newSalaryHistory.setApprovalDate(LocalDate.now());

        mockMvc.perform(put("/api/employees/{id}/salary", testEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSalaryHistory)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newSalary", is(75000.00)))
                .andExpect(jsonPath("$.previousSalary", is(70000.00)))
                .andExpect(jsonPath("$.changeReason", is("PROMOTION")));
    }

    @Test
    void processSalaryAdjustment_WithValidData_ShouldProcessAdjustment() throws Exception {
        SalaryHistory salaryAdjustment = new SalaryHistory();
        salaryAdjustment.setNewSalary(new BigDecimal("72000.00"));
        salaryAdjustment.setEffectiveDate(LocalDate.now());
        salaryAdjustment.setChangeReason(SalaryChangeReason.MARKET_ADJUSTMENT);
        salaryAdjustment.setNotes("Market adjustment for competitive salary");

        mockMvc.perform(post("/api/payroll/salary-adjustment")
                .param("employeeId", testEmployee.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(salaryAdjustment)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newSalary", is(72000.00)))
                .andExpect(jsonPath("$.changeReason", is("MARKET_ADJUSTMENT")));
    }

    // ==================== BONUS TESTS ====================

    @Test
    void getBonuses_ForEmployee_ShouldReturnBonuses() throws Exception {
        mockMvc.perform(get("/api/payroll/bonuses")
                .param("employeeId", testEmployee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bonusType", is("PERFORMANCE")))
                .andExpect(jsonPath("$[0].amount", is(5000.00)))
                .andExpect(jsonPath("$[0].status", is("APPROVED")));
    }

    @Test
    void getBonusByStatus_ShouldReturnFilteredBonuses() throws Exception {
        mockMvc.perform(get("/api/payroll/bonuses")
                .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("APPROVED")));
    }

    @Test
    void createBonus_WithValidData_ShouldCreateBonus() throws Exception {
        Bonus newBonus = new Bonus();
        newBonus.setBonusType(BonusType.ANNUAL);
        newBonus.setAmount(new BigDecimal("3000.00"));
        newBonus.setDescription("Annual bonus for 2023");
        newBonus.setAwardDate(LocalDate.now());
        newBonus.setStatus(BonusStatus.PENDING);

        mockMvc.perform(post("/api/payroll/bonuses")
                .param("employeeId", testEmployee.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBonus)))
                .andDo(print())  // Add this to see the detailed response
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bonusType", is("ANNUAL")))
                .andExpect(jsonPath("$.amount", is(3000.00)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void getBonusById_WhenBonusExists_ShouldReturnBonus() throws Exception {
        mockMvc.perform(get("/api/payroll/bonuses/{id}", testBonus.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bonusType", is("PERFORMANCE")))
                .andExpect(jsonPath("$.amount", is(5000.00)))
                .andExpect(jsonPath("$.description", is("Q4 Performance Bonus")));
    }

    @Test
    void updateBonus_WithValidData_ShouldUpdateBonus() throws Exception {
        Bonus updatedBonus = new Bonus();
        updatedBonus.setBonusType(BonusType.PERFORMANCE);
        updatedBonus.setAmount(new BigDecimal("5500.00"));
        updatedBonus.setDescription("Updated Q4 Performance Bonus");
        updatedBonus.setAwardDate(testBonus.getAwardDate());
        updatedBonus.setStatus(BonusStatus.APPROVED);
        updatedBonus.setApprovedBy("Updated Manager");
        updatedBonus.setApprovalDate(LocalDate.now());

        mockMvc.perform(put("/api/payroll/bonuses/{id}", testBonus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBonus)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount", is(5500.00)))
                .andExpect(jsonPath("$.description", is("Updated Q4 Performance Bonus")))
                .andExpect(jsonPath("$.approvedBy", is("Updated Manager")));
    }

    @Test
    void approveBonus_ShouldApproveBonus() throws Exception {
        // Create a pending bonus first
        Bonus pendingBonus = new Bonus();
        pendingBonus.setEmployee(testEmployee);
        pendingBonus.setBonusType(BonusType.SPOT);
        pendingBonus.setAmount(new BigDecimal("1000.00"));
        pendingBonus.setDescription("Spot bonus");
        pendingBonus.setAwardDate(LocalDate.now());
        pendingBonus.setStatus(BonusStatus.PENDING);
        pendingBonus = bonusRepository.save(pendingBonus);

        mockMvc.perform(put("/api/payroll/bonuses/{id}/approve", pendingBonus.getId())
                .param("approvedBy", "Test Manager"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approvedBy", is("Test Manager")));
    }

    @Test
    void markBonusAsPaid_ShouldMarkBonusAsPaid() throws Exception {
        mockMvc.perform(put("/api/payroll/bonuses/{id}/pay", testBonus.getId())
                .param("paymentDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("PAID")))
                .andExpect(jsonPath("$.paymentDate", notNullValue()));
    }

    // ==================== DEDUCTION TESTS ====================

    @Test
    void getDeductions_ForEmployee_ShouldReturnDeductions() throws Exception {
        mockMvc.perform(get("/api/payroll/deductions")
                .param("employeeId", testEmployee.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deductionType", is("HEALTH_INSURANCE")))
                .andExpect(jsonPath("$[0].amount", is(200.00)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    void getActiveDeductions_ForEmployee_ShouldReturnActiveDeductions() throws Exception {
        mockMvc.perform(get("/api/payroll/deductions")
                .param("employeeId", testEmployee.getId().toString())
                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    void createDeduction_WithValidData_ShouldCreateDeduction() throws Exception {
        Deduction newDeduction = new Deduction();
        newDeduction.setDeductionType(DeductionType.RETIREMENT_401K);
        newDeduction.setPercentage(new BigDecimal("5.00"));
        newDeduction.setDescription("401k contribution");
        newDeduction.setEffectiveDate(LocalDate.now());
        newDeduction.setStatus(DeductionStatus.ACTIVE);
        newDeduction.setIsPreTax(true);
        newDeduction.setIsMandatory(false);
        newDeduction.setFrequency("MONTHLY");

        mockMvc.perform(post("/api/payroll/deductions")
                .param("employeeId", testEmployee.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDeduction)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deductionType", is("RETIREMENT_401K")))
                .andExpect(jsonPath("$.percentage", is(5.00)))
                .andExpect(jsonPath("$.isPreTax", is(true)));
    }

    @Test
    void getDeductionById_WhenDeductionExists_ShouldReturnDeduction() throws Exception {
        mockMvc.perform(get("/api/payroll/deductions/{id}", testDeduction.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deductionType", is("HEALTH_INSURANCE")))
                .andExpect(jsonPath("$.amount", is(200.00)))
                .andExpect(jsonPath("$.description", is("Monthly health insurance premium")));
    }

    @Test
    void updateDeduction_WithValidData_ShouldUpdateDeduction() throws Exception {
        Deduction updatedDeduction = new Deduction();
        updatedDeduction.setDeductionType(DeductionType.HEALTH_INSURANCE);
        updatedDeduction.setAmount(new BigDecimal("250.00"));
        updatedDeduction.setDescription("Updated monthly health insurance premium");
        updatedDeduction.setEffectiveDate(testDeduction.getEffectiveDate());
        updatedDeduction.setStatus(DeductionStatus.ACTIVE);
        updatedDeduction.setIsPreTax(true);
        updatedDeduction.setIsMandatory(false);
        updatedDeduction.setFrequency("MONTHLY");

        mockMvc.perform(put("/api/payroll/deductions/{id}", testDeduction.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDeduction)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount", is(250.00)))
                .andExpect(jsonPath("$.description", is("Updated monthly health insurance premium")));
    }

    // ==================== COMPENSATION SUMMARY TESTS ====================

    @Test
    void getCompensationSummary_ShouldReturnCompensationDetails() throws Exception {
        mockMvc.perform(get("/api/employees/{id}/compensation-summary", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currentSalary", is(70000.00)))
                .andExpect(jsonPath("$.totalBonuses", is(5000.00)))
                .andExpect(jsonPath("$.totalDeductions", is(200.00)))
                .andExpect(jsonPath("$.grossCompensation", is(75000.00)))
                .andExpect(jsonPath("$.netCompensation", is(74800.00)))
                .andExpect(jsonPath("$.activeDeductions", hasSize(1)));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void getPayGradeById_WhenPayGradeNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/payroll/pay-grades/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBonusById_WhenBonusNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/payroll/bonuses/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDeductionById_WhenDeductionNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/payroll/deductions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayGrade_WithInvalidData_ShouldReturn400() throws Exception {
        PayGrade invalidPayGrade = new PayGrade();
        // Missing required fields

        mockMvc.perform(post("/api/payroll/pay-grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayGrade)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBonus_WithInvalidAmount_ShouldReturn400() throws Exception {
        Bonus invalidBonus = new Bonus();
        invalidBonus.setBonusType(BonusType.PERFORMANCE);
        invalidBonus.setAmount(new BigDecimal("-1000.00")); // Invalid negative amount
        invalidBonus.setAwardDate(LocalDate.now());

        mockMvc.perform(post("/api/payroll/bonuses")
                .param("employeeId", testEmployee.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBonus)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletePayGrade_WhenInUse_ShouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/payroll/pay-grades/{id}", testPayGrade.getId()))
                .andExpect(status().isBadRequest());
    }
}