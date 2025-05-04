package org.springframework.samples.petclinic.billing.web;

import org.springframework.samples.petclinic.billing.config.CustomerClient;
import org.springframework.samples.petclinic.billing.config.VisitClient;
import org.springframework.samples.petclinic.billing.dto.CustomerDto;
import org.springframework.samples.petclinic.billing.dto.VisitDto;
import org.springframework.samples.petclinic.billing.model.Bill;
import org.springframework.samples.petclinic.billing.model.BillRepository;
import org.springframework.samples.petclinic.billing.model.BillStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final CustomerClient customerClient;
    private final VisitClient visitClient;

    public BillService(BillRepository billRepository, CustomerClient customerClient, VisitClient visitClient) {
        this.billRepository = billRepository;
        this.customerClient = customerClient;
        this.visitClient = visitClient;
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public Optional<Bill> findById(Long id) {
        return billRepository.findById(id);
    }

    public Bill createBill(Long customerId, Long visitId, String description) {
        CustomerDto customer = customerClient.getCustomerById(customerId);
        VisitDto visit = visitClient.getVisitById(visitId);

        Bill bill = new Bill();
        bill.setCustomerId(customerId);
        bill.setVisitId(visitId);
        bill.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        bill.setVisitDate(visit.getDate());
        bill.setAmount(visit.getPrice());
        bill.setDescription(description);
        bill.setIssueDate(LocalDate.now());
        bill.setStatus(BillStatus.OPEN);

        return billRepository.save(bill);
    }

    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    public void deleteById(Long id) {
        billRepository.deleteById(id);
    }
}
