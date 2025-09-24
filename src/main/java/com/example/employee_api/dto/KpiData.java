package com.example.employee_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class KpiData {
    private String name;
    private String description;
    private BigDecimal value;
    private String unit;
    private BigDecimal target;
    private BigDecimal previousValue;
    private Double changePercentage;
    private String trend; // UP, DOWN, STABLE
    private LocalDate calculatedDate;
    private String category;
    private String status; // GOOD, WARNING, CRITICAL

    // Constructors
    public KpiData() {}

    public KpiData(String name, BigDecimal value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.calculatedDate = LocalDate.now();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getTarget() {
        return target;
    }

    public void setTarget(BigDecimal target) {
        this.target = target;
    }

    public BigDecimal getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(BigDecimal previousValue) {
        this.previousValue = previousValue;
    }

    public Double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(Double changePercentage) {
        this.changePercentage = changePercentage;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public LocalDate getCalculatedDate() {
        return calculatedDate;
    }

    public void setCalculatedDate(LocalDate calculatedDate) {
        this.calculatedDate = calculatedDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}