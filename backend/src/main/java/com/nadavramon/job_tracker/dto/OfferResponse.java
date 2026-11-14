package com.nadavramon.job_tracker.dto;

import java.util.UUID;
import java.util.UUID;
import java.lang.String;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import com.nadavramon.job_tracker.enums.Currency;
import java.lang.String;
import java.lang.String;
import java.lang.String;
import java.lang.Integer;
import java.time.LocalDate;
import java.lang.String;

public class OfferResponse {

    private UUID id;
    private UUID applicationId;
    private String companyName;
    private String jobRole;
    private java.math.BigDecimal baseSalary;
    private java.math.BigDecimal bonus;
    private java.math.BigDecimal equity;
    private Currency currency;
    private String location;
    private String remotePolicy;
    private String insurance;
    private Integer ptoDays;
    private java.time.LocalDate startDate;
    private String notes;

    public OfferResponse(UUID id, UUID applicationId, String companyName, String jobRole, java.math.BigDecimal baseSalary, java.math.BigDecimal bonus, java.math.BigDecimal equity, Currency currency, String location, String remotePolicy, String insurance, Integer ptoDays, java.time.LocalDate startDate, String notes) {
        this.id = id;
        this.applicationId = applicationId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.equity = equity;
        this.currency = currency;
        this.location = location;
        this.remotePolicy = remotePolicy;
        this.insurance = insurance;
        this.ptoDays = ptoDays;
        this.startDate = startDate;
        this.notes = notes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public java.math.BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public java.math.BigDecimal getBonus() {
        return bonus;
    }

    public java.math.BigDecimal getEquity() {
        return equity;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getLocation() {
        return location;
    }

    public String getRemotePolicy() {
        return remotePolicy;
    }

    public String getInsurance() {
        return insurance;
    }

    public Integer getPtoDays() {
        return ptoDays;
    }

    public java.time.LocalDate getStartDate() {
        return startDate;
    }

    public String getNotes() {
        return notes;
    }

}
