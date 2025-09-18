package com.example.employee_api.model;

import com.example.employee_api.model.enums.DepartmentStatus;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import com.example.employee_api.model.enums.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class LeaveManagementEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    private Employee employee;
    private Employee manager;
    private Department department;
    private Position position;
    private LeaveType leaveType;

    @BeforeEach
    void setUp() {
        // Create test department
        department = new Department();
        department.setDepartmentCode("ENG");
        department.setName("Engineering");
        department.setDescription("Software Engineering Department");
        department.setLocation("Building A");
        department.setStatus(DepartmentStatus.ACTIVE);
        department.setEmail("engineering@company.com");
        department.setPhone("+1234567890");
        department = entityManager.persistAndFlush(department);

        // Create test position
        position = new Position();
        position.setTitle("Software Engineer");
        position.setDescription("Senior Software Engineer");
        position.setLevel(PositionLevel.SENIOR);
        position.setStatus(PositionStatus.ACTIVE);
        position.setDepartment(department);
        position.setMinSalary(BigDecimal.valueOf(80000));
        position.setMaxSalary(BigDecimal.valueOf(120000));
        position = entityManager.persistAndFlush(position);

        // Create manager employee
        manager = new Employee();
        manager.setFirstName("Jane");
        manager.setLastName("Manager");
        manager.setEmail("jane.manager@company.com");
        manager.setPhone("+1234567891");
        manager.setEmployeeId("MGR001");
        manager.setJobTitle("Engineering Manager");
        manager.setHireDate(LocalDate.now().minusYears(3));
        manager.setDepartment(department);
        manager.setCurrentPosition(position);
        manager.setSalary(BigDecimal.valueOf(150000));
        manager.setGender("FEMALE");
        manager.setStatus(EmployeeStatus.ACTIVE);
        manager = entityManager.persistAndFlush(manager);

        // Create test employee
        employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@company.com");
        employee.setPhone("+1234567890");
        employee.setEmployeeId("EMP001");
        employee.setJobTitle("Software Engineer");
        employee.setHireDate(LocalDate.now().minusYears(1));
        employee.setDepartment(department);
        employee.setCurrentPosition(position);
        employee.setSalary(BigDecimal.valueOf(100000));
        employee.setGender("MALE");
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee = entityManager.persistAndFlush(employee);

        // Create test leave type
        leaveType = new LeaveType();
        leaveType.setName("Annual Leave");
        leaveType.setDescription("Yearly vacation leave");
        leaveType.setDaysAllowed(25);
        leaveType.setRequiresApproval(true);
        leaveType.setCarryForward(true);
        leaveType.setActive(true);
        leaveType = entityManager.persistAndFlush(leaveType);
    }

    @Test
    void testLeaveBalanceCreation() {
        // Given
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveType(leaveType);
        leaveBalance.setYear(2025);
        leaveBalance.setAllocatedDays(25.0);
        leaveBalance.setUsedDays(5.0);

        // When
        LeaveBalance saved = entityManager.persistAndFlush(leaveBalance);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmployee()).isEqualTo(employee);
        assertThat(saved.getLeaveType()).isEqualTo(leaveType);
        assertThat(saved.getYear()).isEqualTo(2025);
        assertThat(saved.getAllocatedDays()).isEqualTo(25.0);
        assertThat(saved.getUsedDays()).isEqualTo(5.0);
        assertThat(saved.getRemainingDays()).isEqualTo(20.0);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void testLeaveBalanceRemainingDaysCalculation() {
        // Given
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveType(leaveType);
        leaveBalance.setYear(2025);
        leaveBalance.setAllocatedDays(30.0);
        leaveBalance.setUsedDays(12.0);

        // When
        leaveBalance.calculateRemainingDays();

        // Then
        assertThat(leaveBalance.getRemainingDays()).isEqualTo(18.0);
    }

    @Test
    void testLeaveRequestCreation() {
        // Given
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(7));
        leaveRequest.setEndDate(LocalDate.now().plusDays(11));
        leaveRequest.setTotalDays(5.0);
        leaveRequest.setReason("Family vacation");
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);

        // When
        LeaveRequest saved = entityManager.persistAndFlush(leaveRequest);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmployee()).isEqualTo(employee);
        assertThat(saved.getLeaveType()).isEqualTo(leaveType);
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.now().plusDays(11));
        assertThat(saved.getTotalDays()).isEqualTo(5.0);
        assertThat(saved.getReason()).isEqualTo("Family vacation");
        assertThat(saved.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void testLeaveRequestStatusEnum() {
        // Given
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(1));
        leaveRequest.setEndDate(LocalDate.now().plusDays(3));
        leaveRequest.setTotalDays(3.0);
        leaveRequest.setReason("Medical appointment");

        // Test all status transitions
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.PENDING);

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.APPROVED);

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.REJECTED);

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.CANCELLED);
    }

    @Test
    void testLeaveRequestApprovalFields() {
        // Given
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(1));
        leaveRequest.setEndDate(LocalDate.now().plusDays(2));
        leaveRequest.setTotalDays(2.0);
        leaveRequest.setReason("Personal leave");
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(manager);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        // When
        LeaveRequest saved = entityManager.persistAndFlush(leaveRequest);

        // Then
        assertThat(saved.getApprovedBy()).isEqualTo(manager);
        assertThat(saved.getApprovedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.APPROVED);
    }

    @Test
    void testLeaveRequestRejection() {
        // Given
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(1));
        leaveRequest.setEndDate(LocalDate.now().plusDays(3));
        leaveRequest.setTotalDays(3.0);
        leaveRequest.setReason("Vacation");
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leaveRequest.setRejectionReason("Insufficient leave balance");

        // When
        LeaveRequest saved = entityManager.persistAndFlush(leaveRequest);

        // Then
        assertThat(saved.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.REJECTED);
        assertThat(saved.getRejectionReason()).isEqualTo("Insufficient leave balance");
    }

    @Test
    void testLeaveDocumentCreation() {
        // Given - First create a leave request
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(1));
        leaveRequest.setEndDate(LocalDate.now().plusDays(3));
        leaveRequest.setTotalDays(3.0);
        leaveRequest.setReason("Medical leave");
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        leaveRequest = entityManager.persistAndFlush(leaveRequest);

        // Create leave document
        LeaveDocument document = new LeaveDocument();
        document.setLeaveRequest(leaveRequest);
        document.setDocumentName("medical_certificate_123.pdf");
        document.setDescription("Medical Certificate");
        document.setFilePath("/uploads/documents/medical_certificate_123.pdf");
        document.setFileSize(1024L);
        document.setFileType("application/pdf");

        // When
        LeaveDocument saved = entityManager.persistAndFlush(document);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLeaveRequest()).isEqualTo(leaveRequest);
        assertThat(saved.getDocumentName()).isEqualTo("medical_certificate_123.pdf");
        assertThat(saved.getDescription()).isEqualTo("Medical Certificate");
        assertThat(saved.getFilePath()).isEqualTo("/uploads/documents/medical_certificate_123.pdf");
        assertThat(saved.getFileSize()).isEqualTo(1024L);
        assertThat(saved.getFileType()).isEqualTo("application/pdf");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void testLeaveDocumentMultiplePerRequest() {
        // Given - Create a leave request
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(1));
        leaveRequest.setEndDate(LocalDate.now().plusDays(5));
        leaveRequest.setTotalDays(5.0);
        leaveRequest.setReason("Medical leave with surgery");
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);
        leaveRequest = entityManager.persistAndFlush(leaveRequest);

        // Create multiple documents for the same request
        LeaveDocument doc1 = new LeaveDocument();
        doc1.setLeaveRequest(leaveRequest);
        doc1.setDocumentName("medical_cert_1.pdf");
        doc1.setDescription("Medical Certificate 1");
        doc1.setFilePath("/uploads/documents/medical_cert_1.pdf");
        doc1.setFileSize(2048L);
        doc1.setFileType("application/pdf");

        LeaveDocument doc2 = new LeaveDocument();
        doc2.setLeaveRequest(leaveRequest);
        doc2.setDocumentName("surgery_report.pdf");
        doc2.setDescription("Surgery Report");
        doc2.setFilePath("/uploads/documents/surgery_report.pdf");
        doc2.setFileSize(3072L);
        doc2.setFileType("application/pdf");

        // When
        LeaveDocument saved1 = entityManager.persistAndFlush(doc1);
        LeaveDocument saved2 = entityManager.persistAndFlush(doc2);

        // Then
        assertThat(saved1.getLeaveRequest()).isEqualTo(leaveRequest);
        assertThat(saved2.getLeaveRequest()).isEqualTo(leaveRequest);
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
    }

    @Test
    void testLeaveTypeCreation() {
        // Given
        LeaveType sickLeave = new LeaveType();
        sickLeave.setName("Sick Leave");
        sickLeave.setDescription("Medical leave for illness");
        sickLeave.setDaysAllowed(10);
        sickLeave.setRequiresApproval(false);
        sickLeave.setCarryForward(false);
        sickLeave.setActive(true);

        // When
        LeaveType saved = entityManager.persistAndFlush(sickLeave);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Sick Leave");
        assertThat(saved.getDescription()).isEqualTo("Medical leave for illness");
        assertThat(saved.getDaysAllowed()).isEqualTo(10);
        assertThat(saved.getRequiresApproval()).isFalse();
        assertThat(saved.getCarryForward()).isFalse();
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void testEmployeeLeaveRelationships() {
        // Given - Create multiple leave balances for the same employee
        LeaveType sickLeave = new LeaveType();
        sickLeave.setName("Sick Leave");
        sickLeave.setDescription("Medical leave");
        sickLeave.setDaysAllowed(10);
        sickLeave.setRequiresApproval(false);
        sickLeave.setCarryForward(false);
        sickLeave.setActive(true);
        sickLeave = entityManager.persistAndFlush(sickLeave);

        LeaveBalance annualBalance = new LeaveBalance();
        annualBalance.setEmployee(employee);
        annualBalance.setLeaveType(leaveType);
        annualBalance.setYear(2025);
        annualBalance.setAllocatedDays(25.0);
        annualBalance.setUsedDays(5.0);

        LeaveBalance sickBalance = new LeaveBalance();
        sickBalance.setEmployee(employee);
        sickBalance.setLeaveType(sickLeave);
        sickBalance.setYear(2025);
        sickBalance.setAllocatedDays(10.0);
        sickBalance.setUsedDays(2.0);

        // When
        LeaveBalance savedAnnual = entityManager.persistAndFlush(annualBalance);
        LeaveBalance savedSick = entityManager.persistAndFlush(sickBalance);

        // Then
        assertThat(savedAnnual.getEmployee()).isEqualTo(employee);
        assertThat(savedSick.getEmployee()).isEqualTo(employee);
        assertThat(savedAnnual.getLeaveType()).isEqualTo(leaveType);
        assertThat(savedSick.getLeaveType()).isEqualTo(sickLeave);
        assertThat(savedAnnual.getRemainingDays()).isEqualTo(20.0);
        assertThat(savedSick.getRemainingDays()).isEqualTo(8.0);
    }

    @Test
    void testLeaveRequestHalfDayFunctionality() {
        // Given
        LeaveRequest halfDayRequest = new LeaveRequest();
        halfDayRequest.setEmployee(employee);
        halfDayRequest.setLeaveType(leaveType);
        halfDayRequest.setStartDate(LocalDate.now().plusDays(1));
        halfDayRequest.setEndDate(LocalDate.now().plusDays(1));
        halfDayRequest.setTotalDays(0.5);
        halfDayRequest.setHalfDay(true);
        halfDayRequest.setHalfDayPeriod(LeaveRequest.HalfDayPeriod.MORNING);
        halfDayRequest.setReason("Medical appointment");
        halfDayRequest.setStatus(LeaveRequest.LeaveStatus.PENDING);

        // When
        LeaveRequest saved = entityManager.persistAndFlush(halfDayRequest);

        // Then
        assertThat(saved.getHalfDay()).isTrue();
        assertThat(saved.getHalfDayPeriod()).isEqualTo(LeaveRequest.HalfDayPeriod.MORNING);
        assertThat(saved.getTotalDays()).isEqualTo(0.5);
    }

    @Test
    void testLeaveBalanceCarryForward() {
        // Given - Previous year balance with carry forward
        LeaveBalance previousYearBalance = new LeaveBalance();
        previousYearBalance.setEmployee(employee);
        previousYearBalance.setLeaveType(leaveType);
        previousYearBalance.setYear(2024);
        previousYearBalance.setAllocatedDays(25.0);
        previousYearBalance.setUsedDays(20.0);
        previousYearBalance.setCarryForwardDays(3.0);
        previousYearBalance = entityManager.persistAndFlush(previousYearBalance);

        // Current year balance with carry forward from previous year
        LeaveBalance currentYearBalance = new LeaveBalance();
        currentYearBalance.setEmployee(employee);
        currentYearBalance.setLeaveType(leaveType);
        currentYearBalance.setYear(2025);
        currentYearBalance.setAllocatedDays(25.0);
        currentYearBalance.setUsedDays(10.0);
        currentYearBalance.setCarryForwardDays(3.0);

        // When
        LeaveBalance saved = entityManager.persistAndFlush(currentYearBalance);

        // Then
        assertThat(saved.getCarryForwardDays()).isEqualTo(3.0);
        // Total available = allocated + carry forward = 25.0 + 3.0 = 28.0
        assertThat(saved.getAllocatedDays() + saved.getCarryForwardDays()).isEqualTo(28.0);
        assertThat(saved.getRemainingDays()).isEqualTo(18.0); // 28 total - 10 used
    }
}