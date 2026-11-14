package com.nadavramon.job_tracker.dto;

import com.nadavramon.job_tracker.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
public class OfferRequest {

    @NotNull
    @DecimalMin(value = "0.00")
    private java.math.BigDecimal baseSalary;

    @DecimalMin(value = "0.00")
    private java.math.BigDecimal bonus;

    @DecimalMin(value = "0.00")
    private java.math.BigDecimal equity;

    @NotNull
    private Currency currency;

    @NotBlank
    @Size(max = 255)
    private String location;

    private String remotePolicy;

    private String insurance;

    @Min(0)
    private Integer ptoDays;

    private java.time.LocalDate startDate;

    private String notes;

    public java.math.BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(java.math.BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public java.math.BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(java.math.BigDecimal bonus) {
        this.bonus = bonus;
    }

    public java.math.BigDecimal getEquity() {
        return equity;
    }

    public void setEquity(java.math.BigDecimal equity) {
        this.equity = equity;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRemotePolicy() {
        return remotePolicy;
    }

    public void setRemotePolicy(String remotePolicy) {
        this.remotePolicy = remotePolicy;
    }

    public String getInsurance() {
        return insurance;
    }

    public void setInsurance(String insurance) {
        this.insurance = insurance;
    }

    public Integer getPtoDays() {
        return ptoDays;
    }

    public void setPtoDays(Integer ptoDays) {
        this.ptoDays = ptoDays;
    }

    public java.time.LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(java.time.LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
