package com.example.employee_api.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeaveBalance Entity Tests")
class LeaveBalanceTest {

    private Employee employee;
    private LeaveType leaveType;
    private LeaveBalance leaveBalance;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");

        leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Annual Leave");
        leaveType.setDaysAllowed(25);

        leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveType(leaveType);
        leaveBalance.setYear(2025);
        leaveBalance.setAllocatedDays(25.0);
        leaveBalance.setUsedDays(5.0);
    }

    @Test
    @DisplayName("Should calculate remaining days correctly")
    void shouldCalculateRemainingDaysCorrectly() {
        // When
        leaveBalance.calculateRemainingDays();

        // Then
        assertEquals(20.0, leaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should handle zero used days")
    void shouldHandleZeroUsedDays() {
        // Given
        leaveBalance.setUsedDays(0.0);

        // When
        leaveBalance.calculateRemainingDays();

        // Then
        assertEquals(25.0, leaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should handle all days used")
    void shouldHandleAllDaysUsed() {
        // Given
        leaveBalance.setUsedDays(25.0);

        // When
        leaveBalance.calculateRemainingDays();

        // Then
        assertEquals(0.0, leaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should handle negative remaining days")
    void shouldHandleNegativeRemainingDays() {
        // Given
        leaveBalance.setUsedDays(30.0);

        // When
        leaveBalance.calculateRemainingDays();

        // Then
        assertEquals(-5.0, leaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should handle carry forward days")
    void shouldHandleCarryForwardDays() {
        // Given
        leaveBalance.setCarryForwardDays(5.0);
        leaveBalance.setUsedDays(10.0);

        // When
        leaveBalance.calculateRemainingDays();

        // Then - 25 allocated + 5 carry forward - 10 used = 20
        assertEquals(20.0, leaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should handle pending days")
    void shouldHandlePendingDays() {
        // Given
        leaveBalance.setPendingDays(3.0);
        leaveBalance.setUsedDays(5.0);

        // When
        leaveBalance.calculateRemainingDays();

        // Then - 25 allocated - 5 used - 3 pending = 17
        assertEquals(17.0, leaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should set audit fields correctly")
    void shouldSetAuditFieldsCorrectly() {
        // Given
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // When
        leaveBalance.setCreatedAt(now);
        leaveBalance.setUpdatedAt(now);

        // Then
        assertEquals(now, leaveBalance.getCreatedAt());
        assertEquals(now, leaveBalance.getUpdatedAt());
    }

    @Test
    @DisplayName("Should validate required fields")
    void shouldValidateRequiredFields() {
        // Given
        LeaveBalance newLeaveBalance = new LeaveBalance();

        // Then
        assertNull(newLeaveBalance.getEmployee());
        assertNull(newLeaveBalance.getLeaveType());
        assertNull(newLeaveBalance.getYear());
        assertNull(newLeaveBalance.getAllocatedDays());
        assertEquals(0.0, newLeaveBalance.getUsedDays());
        assertEquals(0.0, newLeaveBalance.getPendingDays());
        assertEquals(0.0, newLeaveBalance.getCarryForwardDays());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        // Given
        LeaveType sickLeave = new LeaveType();
        sickLeave.setId(2L);
        sickLeave.setName("Sick Leave");
        sickLeave.setDaysAllowed(10);

        LeaveBalance newBalance = new LeaveBalance();
        newBalance.setId(1L);
        newBalance.setEmployee(employee);
        newBalance.setLeaveType(sickLeave);
        newBalance.setYear(2025);
        newBalance.setAllocatedDays(10.0);
        newBalance.setUsedDays(3.0);
        newBalance.setCarryForwardDays(2.0);
        newBalance.setPendingDays(1.0);
        newBalance.setRemainingDays(8.0);

        // Then
        assertEquals(1L, newBalance.getId());
        assertEquals(employee, newBalance.getEmployee());
        assertEquals(sickLeave, newBalance.getLeaveType());
        assertEquals(2025, newBalance.getYear());
        assertEquals(10.0, newBalance.getAllocatedDays());
        assertEquals(3.0, newBalance.getUsedDays());
        assertEquals(2.0, newBalance.getCarryForwardDays());
        assertEquals(1.0, newBalance.getPendingDays());
        assertEquals(8.0, newBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should test constructor with parameters")
    void shouldTestConstructorWithParameters() {
        // When
        LeaveBalance balance = new LeaveBalance(employee, leaveType, 2025, 30.0);

        // Then
        assertEquals(employee, balance.getEmployee());
        assertEquals(leaveType, balance.getLeaveType());
        assertEquals(2025, balance.getYear());
        assertEquals(30.0, balance.getAllocatedDays());
        assertEquals(30.0, balance.getRemainingDays()); // Should be calculated
    }

    @Test
    @DisplayName("Should recalculate when allocated days change")
    void shouldRecalculateWhenAllocatedDaysChange() {
        // Given
        leaveBalance.setUsedDays(10.0);
        leaveBalance.calculateRemainingDays();
        assertEquals(15.0, leaveBalance.getRemainingDays());

        // When
        leaveBalance.setAllocatedDays(30.0);

        // Then - should auto-recalculate
        assertEquals(20.0, leaveBalance.getRemainingDays());
    }

    @Test
    @DisplayName("Should recalculate when used days change")
    void shouldRecalculateWhenUsedDaysChange() {
        // Given
        leaveBalance.calculateRemainingDays();
        assertEquals(20.0, leaveBalance.getRemainingDays());

        // When
        leaveBalance.setUsedDays(15.0);

        // Then - should auto-recalculate
        assertEquals(10.0, leaveBalance.getRemainingDays());
    }
}