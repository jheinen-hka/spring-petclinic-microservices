package org.springframework.samples.petclinic.billing.web;

import org.springframework.samples.petclinic.billing.model.Bill;
import org.springframework.samples.petclinic.billing.model.BillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public Optional<Bill> findById(Long id) {
        return billRepository.findById(id);
    }

    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    public void deleteById(Long id) {
        billRepository.deleteById(id);
    }
}
