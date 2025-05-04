package org.springframework.samples.petclinic.billing.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.samples.petclinic.billing.dto.CustomerDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customers-service")
public interface CustomerClient {
    @GetMapping("/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);
}
