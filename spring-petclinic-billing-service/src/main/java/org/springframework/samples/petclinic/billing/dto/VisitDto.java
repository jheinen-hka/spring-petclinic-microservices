package org.springframework.samples.petclinic.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public class VisitDto {
    private Long id;
    // The visit service uses Date, but our billing service uses LocalDate, so we need to handle the conversion
    private Date date;
    private BigDecimal price;
    private String description;
    private Integer petId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        // Convert Date to LocalDate if date exists, otherwise return today's date
        return date != null ? new java.sql.Date(date.getTime()).toLocalDate() : LocalDate.now();
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        // If price is not set in the Visit service, use a default price based on the description
        if (price == null) {
            // Simple logic to assign a price based on the description
            if (description != null) {
                String lowerDesc = description.toLowerCase();
                if (lowerDesc.contains("rabies")) {
                    return new BigDecimal("89.99");
                } else if (lowerDesc.contains("neuter")) {
                    return new BigDecimal("129.50");
                } else if (lowerDesc.contains("spay")) {
                    return new BigDecimal("149.99");
                } else if (lowerDesc.contains("check") || lowerDesc.contains("exam")) {
                    return new BigDecimal("59.99");
                }
            }
            // Default price if no match
            return new BigDecimal("45.00");
        }
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPetId() {
        return petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }
}
