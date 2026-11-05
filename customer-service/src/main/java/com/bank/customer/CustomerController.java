package com.bank.customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController @RequestMapping("/api/v1/customers")
public class CustomerController {
 private final CustomerRepository repo;
 public CustomerController(CustomerRepository repo){this.repo=repo;}
 public record CustomerRequest(@NotBlank @Size(max=60) String firstName,@NotBlank @Size(max=60) String lastName,
  @NotNull @Past LocalDate dateOfBirth,@NotBlank @Email String email,@NotBlank String phoneNumber,@NotBlank String address){}
 public record KycRequest(@NotNull KycStatus kycStatus){}
 @PostMapping public ResponseEntity<?> create(@Valid @RequestBody CustomerRequest r){
  Customer c=new Customer(); c.setFirstName(r.firstName());c.setLastName(r.lastName());c.setDateOfBirth(r.dateOfBirth());
  c.setEmail(r.email().toLowerCase());c.setPhoneNumber(r.phoneNumber());c.setAddress(r.address());
  return ResponseEntity.status(201).body(repo.save(c));
 }
 @GetMapping("/{id}") public ResponseEntity<?> get(@PathVariable Long id){return repo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElseGet(()->ResponseEntity.status(404).body(Map.of("message","Customer not found")));}
 @GetMapping public Page<Customer> all(@PageableDefault(size=10,sort="id") Pageable p){return repo.findAll(p);}
 @PutMapping("/{id}")
 public ResponseEntity<?> update(
         @PathVariable Long id,
         @Valid @RequestBody CustomerRequest r) {

  return repo.findById(id)
          .<ResponseEntity<?>>map(c -> {
           c.setFirstName(r.firstName());
           c.setLastName(r.lastName());
           c.setDateOfBirth(r.dateOfBirth());
           c.setEmail(r.email().toLowerCase());
           c.setPhoneNumber(r.phoneNumber());
           c.setAddress(r.address());

           return ResponseEntity.ok(repo.save(c));
          })
          .orElseGet(() ->
                  ResponseEntity.status(404)
                          .body(Map.of("message", "Customer not found")));
 }
 @PatchMapping("/{id}/kyc-status")
 public ResponseEntity<?> kyc(
         @PathVariable Long id,
         @Valid @RequestBody KycRequest r) {

  return repo.findById(id)
          .<ResponseEntity<?>>map(c -> {
           c.setKycStatus(r.kycStatus());

           return ResponseEntity.ok(repo.save(c));
          })
          .orElseGet(() ->
                  ResponseEntity.status(404)
                          .body(Map.of("message", "Customer not found")));
 }
}
