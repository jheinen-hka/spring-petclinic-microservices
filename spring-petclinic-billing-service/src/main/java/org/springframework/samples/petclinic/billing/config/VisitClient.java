package org.springframework.samples.petclinic.billing.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.samples.petclinic.billing.dto.VisitDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "visits-service")
public interface VisitClient {
    @GetMapping("/visits/{id}")
    VisitDto getVisitById(@PathVariable("id") Integer id);
}
