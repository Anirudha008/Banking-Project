package com.bank.loan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.redisson.api.RedissonClient;
import org.redisson.api.RLock;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController @RequestMapping("/api/v1/loans")
public class LoanController {
 private final LoanRepository loans;private final RepaymentRepository repayments;private final RedissonClient redisson;
 public LoanController(LoanRepository l,RepaymentRepository r,RedissonClient redisson){loans=l;repayments=r;this.redisson=redisson;}
 public record ApplyRequest(@NotNull Long customerId,@NotNull Long accountId,@NotNull @DecimalMin("1.00") BigDecimal principal,
  @NotNull @DecimalMin("0.00") BigDecimal annualInterestRate,@NotNull @Min(1) @Max(360) Integer termMonths,
  @NotNull @DecimalMin("1.00") BigDecimal monthlyIncome,@NotNull @Min(300) @Max(900) Integer creditScore){}
 public record RepaymentRequest(@NotNull @DecimalMin(value="0.00",inclusive=false) BigDecimal amount){}
 @PostMapping public ResponseEntity<?> apply(@Valid @RequestBody ApplyRequest r){
  Loan loan=new Loan(r.customerId(),r.accountId(),r.principal(),r.annualInterestRate(),r.termMonths());
  // POC eligibility rule: credit score >= 650 and principal <= 20x monthly income.
  if(r.creditScore()<650||r.principal().compareTo(r.monthlyIncome().multiply(BigDecimal.valueOf(20)))>0)loan.setStatus(LoanStatus.REJECTED);
  return ResponseEntity.status(201).body(loans.save(loan));
 }
 @GetMapping("/{id}") public Loan get(@PathVariable Long id){return loans.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Loan not found"));}
 @GetMapping public List<Loan> all(){return loans.findAll();}
 @PostMapping("/{id}/approve") @Transactional public Loan approve(@PathVariable Long id){Loan l=get(id);if(l.getStatus()!=LoanStatus.PENDING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Only pending loans can be approved");l.setStatus(LoanStatus.APPROVED);return loans.save(l);}
 @PostMapping("/{id}/reject") @Transactional public Loan reject(@PathVariable Long id){Loan l=get(id);if(l.getStatus()!=LoanStatus.PENDING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Only pending loans can be rejected");l.setStatus(LoanStatus.REJECTED);return loans.save(l);}
 @PostMapping("/{id}/disburse") @Transactional public Loan disburse(@PathVariable Long id){
  RLock lock=redisson.getLock("loan-disbursement:"+id);
  try{
   if(!lock.tryLock(0,30,TimeUnit.SECONDS))throw new ResponseStatusException(HttpStatus.CONFLICT,"Loan disbursement is already in progress");
   Loan l=get(id);if(l.getStatus()!=LoanStatus.APPROVED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Loan must be approved before disbursement");
   // This POC marks the loan disbursed. Cross-service account credit must be implemented as a Saga.
   l.setStatus(LoanStatus.DISBURSED);return loans.save(l);
  }catch(InterruptedException e){Thread.currentThread().interrupt();throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Interrupted while acquiring disbursement lock");}
  finally{if(lock.isHeldByCurrentThread())lock.unlock();}
 }
 @PostMapping("/{id}/repayments") @Transactional public ResponseEntity<?> repay(@PathVariable Long id,@Valid @RequestBody RepaymentRequest r){
  Loan l=get(id);if(l.getStatus()!=LoanStatus.DISBURSED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Loan is not disbursed");
  if(r.amount().compareTo(l.getOutstanding())>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Repayment exceeds outstanding principal");
  l.setOutstanding(l.getOutstanding().subtract(r.amount()));if(l.getOutstanding().compareTo(BigDecimal.ZERO)==0)l.setStatus(LoanStatus.CLOSED);
  loans.save(l);return ResponseEntity.status(201).body(repayments.save(new Repayment(id,r.amount())));
 }
 @GetMapping("/{id}/repayments") public List<Repayment> repayments(@PathVariable Long id){get(id);return repayments.findByLoanId(id);}
}
