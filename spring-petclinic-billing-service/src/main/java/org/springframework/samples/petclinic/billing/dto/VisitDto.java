package org.springframework.samples.petclinic.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VisitDto {
    private Long id;
    private LocalDate date;
    private BigDecimal price;
    private String description;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
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
}
