package org.springframework.samples.petclinic.billing.web;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.billing.model.Bill;
import org.springframework.samples.petclinic.billing.model.BillStatus;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public List<Bill> getAllBills() {
        return billService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBill(@PathVariable Long id) {
        return billService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Bill> createBill(@RequestParam Long customerId,
                                           @RequestParam Long visitId,
                                           @RequestParam(required = false) String description) {
        Bill bill = billService.createBill(customerId, visitId, description);
        return ResponseEntity.created(URI.create("/bills/" + bill.getId())).body(bill);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Bill> updateStatus(@PathVariable Long id, @RequestBody BillStatus status) {
        return billService.findById(id).map(bill -> {
            bill.setStatus(status);
            billService.save(bill);
            return ResponseEntity.ok(bill);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id) {
        billService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
