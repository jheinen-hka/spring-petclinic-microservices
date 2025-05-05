package org.springframework.samples.petclinic.billing.web;

import jakarta.validation.constraints.NotNull;

/**
 * Request object for creating a new bill.
 */
public class BillRequest {
    
    @NotNull
    private Long customerId;
    
    @NotNull
    private Long visitId;

    // Default constructor for deserialization
    public BillRequest() {
    }
    
    public BillRequest(Long customerId, Long visitId) {
        this.customerId = customerId;
        this.visitId = visitId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }
}