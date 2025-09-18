package com.example.employee_api.repository;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.DepartmentStatus;
import com.example.employee_api.model.enums.PositionLevel;
import com.example.employee_api.model.enums.PositionStatus;
import com.example.employee_api.model.enums.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class LeaveRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveDocumentRepository leaveDocumentRepository;

    private Employee employee1;
    private Employee employee2;
    private Department department;
    private Position position;
    private LeaveType annualLeave;
    private LeaveType sickLeave;

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

        // Create test employees
        employee1 = new Employee();
        employee1.setFirstName("John");
        employee1.setLastName("Doe");
        employee1.setEmail("john.doe@company.com");
        employee1.setPhone("+1234567890");
        employee1.setEmployeeId("EMP001");
        employee1.setJobTitle("Software Engineer");
        employee1.setHireDate(LocalDate.now().minusYears(1));
        employee1.setDepartment(department);
        employee1.setCurrentPosition(position);
        employee1.setSalary(BigDecimal.valueOf(100000));
        employee1.setGender("MALE");
        employee1.setStatus(EmployeeStatus.ACTIVE);
        employee1 = entityManager.persistAndFlush(employee1);

        employee2 = new Employee();
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@company.com");
        employee2.setPhone("+1234567891");
        employee2.setEmployeeId("EMP002");
        employee2.setJobTitle("Senior Software Engineer");
        employee2.setHireDate(LocalDate.now().minusYears(2));
        employee2.setDepartment(department);
        employee2.setCurrentPosition(position);
        employee2.setSalary(BigDecimal.valueOf(120000));
        employee2.setGender("FEMALE");
        employee2.setStatus(EmployeeStatus.ACTIVE);
        employee2 = entityManager.persistAndFlush(employee2);

        // Create test leave types
        annualLeave = new LeaveType();
        annualLeave.setName("Annual Leave");
        annualLeave.setDescription("Yearly vacation leave");
        annualLeave.setDaysAllowed(25);
        annualLeave.setRequiresApproval(true);
        annualLeave.setCarryForward(true);
        annualLeave.setActive(true);
        annualLeave = entityManager.persistAndFlush(annualLeave);

        sickLeave = new LeaveType();
        sickLeave.setName("Sick Leave");
        sickLeave.setDescription("Medical leave");
        sickLeave.setDaysAllowed(10);
        sickLeave.setRequiresApproval(false);
        sickLeave.setCarryForward(false);
        sickLeave.setActive(true);
        sickLeave = entityManager.persistAndFlush(sickLeave);
    }

    @Test
    void testLeaveBalanceRepositoryFindByEmployee() {
        // Given
        LeaveBalance balance1 = new LeaveBalance();
        balance1.setEmployee(employee1);
        balance1.setLeaveType(annualLeave);
        balance1.setYear(2025);
        balance1.setAllocatedDays(25.0);
        balance1.setUsedDays(5.0);
        entityManager.persistAndFlush(balance1);

        LeaveBalance balance2 = new LeaveBalance();
        balance2.setEmployee(employee1);
        balance2.setLeaveType(sickLeave);
        balance2.setYear(2025);
        balance2.setAllocatedDays(10.0);
        balance2.setUsedDays(2.0);
        entityManager.persistAndFlush(balance2);

        // When
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployee(employee1);

        // Then
        assertThat(balances).hasSize(2);
        assertThat(balances).extracting("leaveType").containsExactlyInAnyOrder(annualLeave, sickLeave);
    }

    @Test
    void testLeaveBalanceRepositoryFindByEmployeeAndYear() {
        // Given
        LeaveBalance balance2024 = new LeaveBalance();
        balance2024.setEmployee(employee1);
        balance2024.setLeaveType(annualLeave);
        balance2024.setYear(2024);
        balance2024.setAllocatedDays(25.0);
        balance2024.setUsedDays(15.0);
        entityManager.persistAndFlush(balance2024);

        LeaveBalance balance2025 = new LeaveBalance();
        balance2025.setEmployee(employee1);
        balance2025.setLeaveType(annualLeave);
        balance2025.setYear(2025);
        balance2025.setAllocatedDays(25.0);
        balance2025.setUsedDays(5.0);
        entityManager.persistAndFlush(balance2025);

        // When
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeAndYear(employee1, 2025);

        // Then
        assertThat(balances).hasSize(1);
        assertThat(balances.get(0).getYear()).isEqualTo(2025);
        assertThat(balances.get(0).getUsedDays()).isEqualTo(5.0);
    }

    @Test
    void testLeaveBalanceRepositoryFindByEmployeeAndLeaveTypeAndYear() {
        // Given
        LeaveBalance balance = new LeaveBalance();
        balance.setEmployee(employee1);
        balance.setLeaveType(annualLeave);
        balance.setYear(2025);
        balance.setAllocatedDays(25.0);
        balance.setUsedDays(5.0);
        entityManager.persistAndFlush(balance);

        // When
        Optional<LeaveBalance> foundBalance = leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(employee1, annualLeave, 2025);

        // Then
        assertThat(foundBalance).isPresent();
        assertThat(foundBalance.get().getAllocatedDays()).isEqualTo(25.0);
        assertThat(foundBalance.get().getUsedDays()).isEqualTo(5.0);
    }

    @Test
    void testLeaveRequestRepositoryFindByEmployee() {
        // Given
        LeaveRequest request1 = new LeaveRequest();
        request1.setEmployee(employee1);
        request1.setLeaveType(annualLeave);
        request1.setStartDate(LocalDate.now().plusDays(7));
        request1.setEndDate(LocalDate.now().plusDays(11));
        request1.setTotalDays(5.0);
        request1.setReason("Vacation");
        request1.setStatus(LeaveRequest.LeaveStatus.PENDING);
        entityManager.persistAndFlush(request1);

        LeaveRequest request2 = new LeaveRequest();
        request2.setEmployee(employee1);
        request2.setLeaveType(sickLeave);
        request2.setStartDate(LocalDate.now().plusDays(1));
        request2.setEndDate(LocalDate.now().plusDays(2));
        request2.setTotalDays(2.0);
        request2.setReason("Medical");
        request2.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        entityManager.persistAndFlush(request2);

        // When
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployee(employee1);

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting("status").containsExactlyInAnyOrder(
            LeaveRequest.LeaveStatus.PENDING, 
            LeaveRequest.LeaveStatus.APPROVED
        );
    }

    @Test
    void testLeaveRequestRepositoryFindByStatus() {
        // Given
        LeaveRequest request1 = new LeaveRequest();
        request1.setEmployee(employee1);
        request1.setLeaveType(annualLeave);
        request1.setStartDate(LocalDate.now().plusDays(7));
        request1.setEndDate(LocalDate.now().plusDays(11));
        request1.setTotalDays(5.0);
        request1.setReason("Vacation");
        request1.setStatus(LeaveRequest.LeaveStatus.PENDING);
        entityManager.persistAndFlush(request1);

        LeaveRequest request2 = new LeaveRequest();
        request2.setEmployee(employee2);
        request2.setLeaveType(sickLeave);
        request2.setStartDate(LocalDate.now().plusDays(1));
        request2.setEndDate(LocalDate.now().plusDays(2));
        request2.setTotalDays(2.0);
        request2.setReason("Medical");
        request2.setStatus(LeaveRequest.LeaveStatus.PENDING);
        entityManager.persistAndFlush(request2);

        // When
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findByStatus(LeaveRequest.LeaveStatus.PENDING);

        // Then
        assertThat(pendingRequests).hasSize(2);
        assertThat(pendingRequests).allMatch(req -> req.getStatus() == LeaveRequest.LeaveStatus.PENDING);
    }

    @Test
    void testLeaveRequestRepositoryFindByStartDateBetween() {
        // Given
        LocalDate today = LocalDate.now();
        
        LeaveRequest request1 = new LeaveRequest();
        request1.setEmployee(employee1);
        request1.setLeaveType(annualLeave);
        request1.setStartDate(today.plusDays(5));
        request1.setEndDate(today.plusDays(9));
        request1.setTotalDays(5.0);
        request1.setReason("Vacation");
        request1.setStatus(LeaveRequest.LeaveStatus.PENDING);
        entityManager.persistAndFlush(request1);

        LeaveRequest request2 = new LeaveRequest();
        request2.setEmployee(employee2);
        request2.setLeaveType(sickLeave);
        request2.setStartDate(today.plusDays(15));
        request2.setEndDate(today.plusDays(16));
        request2.setTotalDays(2.0);
        request2.setReason("Medical");
        request2.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        entityManager.persistAndFlush(request2);

        // When
        List<LeaveRequest> requestsInRange = leaveRequestRepository.findByDateRange(
            today.plusDays(1), today.plusDays(10)
        );

        // Then
        assertThat(requestsInRange).hasSize(1);
        assertThat(requestsInRange.get(0).getStartDate()).isEqualTo(today.plusDays(5));
    }

    @Test
    void testLeaveRequestRepositoryFindOverlappingRequests() {
        // Given
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(9);
        
        LeaveRequest request1 = new LeaveRequest();
        request1.setEmployee(employee1);
        request1.setLeaveType(annualLeave);
        request1.setStartDate(startDate.plusDays(2)); // Overlapping
        request1.setEndDate(endDate.plusDays(2));
        request1.setTotalDays(5.0);
        request1.setReason("Vacation");
        request1.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        entityManager.persistAndFlush(request1);

        LeaveRequest request2 = new LeaveRequest();
        request2.setEmployee(employee2);
        request2.setLeaveType(sickLeave);
        request2.setStartDate(startDate.plusDays(10)); // Non-overlapping
        request2.setEndDate(endDate.plusDays(12));
        request2.setTotalDays(3.0);
        request2.setReason("Medical");
        request2.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        entityManager.persistAndFlush(request2);

        // When
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingRequests(
            employee1.getId(), startDate, endDate, List.of(LeaveRequest.LeaveStatus.APPROVED)
        );

        // Then
        assertThat(overlappingRequests).hasSize(1);
        assertThat(overlappingRequests.get(0).getEmployee()).isEqualTo(employee1);
    }

    @Test
    void testLeaveDocumentRepositoryFindByLeaveRequest() {
        // Given
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee1);
        request.setLeaveType(sickLeave);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3.0);
        request.setReason("Medical leave");
        request.setStatus(LeaveRequest.LeaveStatus.PENDING);
        request = entityManager.persistAndFlush(request);

        LeaveDocument doc1 = new LeaveDocument();
        doc1.setLeaveRequest(request);
        doc1.setDocumentName("medical_cert.pdf");
        doc1.setFilePath("/uploads/medical_cert.pdf");
        doc1.setFileType("application/pdf");
        doc1.setFileSize(1024L);
        entityManager.persistAndFlush(doc1);

        LeaveDocument doc2 = new LeaveDocument();
        doc2.setLeaveRequest(request);
        doc2.setDocumentName("doctor_note.pdf");
        doc2.setFilePath("/uploads/doctor_note.pdf");
        doc2.setFileType("application/pdf");
        doc2.setFileSize(512L);
        entityManager.persistAndFlush(doc2);

        // When
        List<LeaveDocument> documents = leaveDocumentRepository.findByLeaveRequest(request);

        // Then
        assertThat(documents).hasSize(2);
        assertThat(documents).extracting("documentName").containsExactlyInAnyOrder("medical_cert.pdf", "doctor_note.pdf");
    }

    @Test
    void testLeaveRequestRepositoryFindByEmployeeAndStatusOrderByCreatedAtDesc() {
        // Given
        LeaveRequest request1 = new LeaveRequest();
        request1.setEmployee(employee1);
        request1.setLeaveType(annualLeave);
        request1.setStartDate(LocalDate.now().plusDays(7));
        request1.setEndDate(LocalDate.now().plusDays(11));
        request1.setTotalDays(5.0);
        request1.setReason("Vacation");
        request1.setStatus(LeaveRequest.LeaveStatus.PENDING);
        entityManager.persistAndFlush(request1);

        // Add a small delay to ensure different creation times
        try { Thread.sleep(10); } catch (InterruptedException e) {}

        LeaveRequest request2 = new LeaveRequest();
        request2.setEmployee(employee1);
        request2.setLeaveType(sickLeave);
        request2.setStartDate(LocalDate.now().plusDays(1));
        request2.setEndDate(LocalDate.now().plusDays(2));
        request2.setTotalDays(2.0);
        request2.setReason("Medical");
        request2.setStatus(LeaveRequest.LeaveStatus.PENDING);
        entityManager.persistAndFlush(request2);

        // When
        List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeAndStatus(
            employee1, LeaveRequest.LeaveStatus.PENDING
        );

        // Then
        assertThat(requests).hasSize(2);
        // Both requests should have PENDING status
        assertThat(requests).allMatch(req -> req.getStatus() == LeaveRequest.LeaveStatus.PENDING);
    }

    @Test
    void testLeaveBalanceRepositoryFindByYear() {
        // Given
        LeaveBalance balance1 = new LeaveBalance();
        balance1.setEmployee(employee1);
        balance1.setLeaveType(annualLeave);
        balance1.setYear(2025);
        balance1.setAllocatedDays(25.0);
        balance1.setUsedDays(5.0);
        entityManager.persistAndFlush(balance1);

        LeaveBalance balance2 = new LeaveBalance();
        balance2.setEmployee(employee2);
        balance2.setLeaveType(annualLeave);
        balance2.setYear(2025);
        balance2.setAllocatedDays(25.0);
        balance2.setUsedDays(10.0);
        entityManager.persistAndFlush(balance2);

        // When
        List<LeaveBalance> yearBalances = leaveBalanceRepository.findByLeaveTypeAndYear(annualLeave, 2025);

        // Then
        assertThat(yearBalances).hasSize(2);
        assertThat(yearBalances).extracting("employee").containsExactlyInAnyOrder(employee1, employee2);
    }

    @Test
    void testLeaveBalanceRepositoryFindLowBalance() {
        // Given
        LeaveBalance lowBalance = new LeaveBalance();
        lowBalance.setEmployee(employee1);
        lowBalance.setLeaveType(annualLeave);
        lowBalance.setYear(2025);
        lowBalance.setAllocatedDays(25.0);
        lowBalance.setUsedDays(23.0); // Only 2 days remaining
        entityManager.persistAndFlush(lowBalance);

        LeaveBalance normalBalance = new LeaveBalance();
        normalBalance.setEmployee(employee2);
        normalBalance.setLeaveType(annualLeave);
        normalBalance.setYear(2025);
        normalBalance.setAllocatedDays(25.0);
        normalBalance.setUsedDays(5.0); // 20 days remaining
        entityManager.persistAndFlush(normalBalance);

        // When - Use employee ID query for low balance check
        List<LeaveBalance> lowBalances = leaveBalanceRepository.findByEmployeeIdAndYearWithInsufficientBalance(
            employee1.getId(), 2025, 5.0
        );

        // Then
        assertThat(lowBalances).hasSize(1);
        assertThat(lowBalances.get(0).getEmployee()).isEqualTo(employee1);
        assertThat(lowBalances.get(0).getRemainingDays()).isEqualTo(2.0);
    }
}