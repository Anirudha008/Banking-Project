package com.bank.account;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.HexFormat;

@RestController @RequestMapping("/api/v1/accounts")
public class AccountController {
 private final AccountRepository accounts; private final TransactionRepository txs;
 private final IdempotencyRepository keys; private final ObjectMapper mapper; private final KafkaTemplate<String,String> kafka;
 public AccountController(AccountRepository a,TransactionRepository t,IdempotencyRepository k,ObjectMapper m,KafkaTemplate<String,String> kafka){accounts=a;txs=t;keys=k;mapper=m;this.kafka=kafka;}
 public record CreateRequest(@NotNull Long customerId,@NotNull AccountType accountType,@NotNull @DecimalMin("0.00") BigDecimal initialDeposit){}
 public record MoneyRequest(@NotNull @DecimalMin(value="0.00",inclusive=false) BigDecimal amount,String description){}
 public record TransferRequest(@NotNull Long sourceAccountId,@NotNull Long targetAccountId,@NotNull @DecimalMin(value="0.00",inclusive=false) BigDecimal amount,String description){}
 @PostMapping @Transactional public ResponseEntity<?> create(@Valid @RequestBody CreateRequest r){
  Account a=new Account();a.setCustomerId(r.customerId());a.setAccountType(r.accountType());a.setBalance(r.initialDeposit());a.setAccountNumber("RB"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase());
  return ResponseEntity.status(201).body(accounts.save(a));
 }
 @GetMapping("/{id}") public Account get(@PathVariable Long id){return accounts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));}
 @GetMapping public List<Account> all(@RequestParam(required=false) Long customerId){return customerId==null?accounts.findAll():accounts.findByCustomerId(customerId);}
 @GetMapping("/{id}/transactions") public List<AccountTransaction> history(@PathVariable Long id){if(!accounts.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");return txs.findByAccountIdOrderByCreatedAtDesc(id);}
 @PostMapping("/{id}/deposit") @Transactional public Account deposit(@PathVariable Long id,@Valid @RequestBody MoneyRequest r){Account a=lock(id);active(a);BigDecimal old=a.getBalance();a.setBalance(old.add(r.amount()));accounts.save(a);txs.save(new AccountTransaction(id,"DEPOSIT",r.amount(),old,a.getBalance(),UUID.randomUUID().toString()));return a;}
 @PostMapping("/{id}/withdraw") @Transactional public Account withdraw(@PathVariable Long id,@Valid @RequestBody MoneyRequest r){Account a=lock(id);active(a);if(a.getBalance().compareTo(r.amount())<0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient funds");BigDecimal old=a.getBalance();a.setBalance(old.subtract(r.amount()));accounts.save(a);txs.save(new AccountTransaction(id,"WITHDRAWAL",r.amount(),old,a.getBalance(),UUID.randomUUID().toString()));return a;}
 @PostMapping("/transfers") @Transactional public ResponseEntity<?> transfer(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody TransferRequest r)throws Exception{
  if(r.sourceAccountId().equals(r.targetAccountId()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Source and target accounts must differ");
  String hash=hash(r.toString());var prior=keys.findById(key);
  if(prior.isPresent()){if(!prior.get().getRequestHash().equals(hash))throw new ResponseStatusException(HttpStatus.CONFLICT,"Idempotency key reused with different request");return ResponseEntity.ok(mapper.readTree(prior.get().getResponseJson()));}
  Long first=Math.min(r.sourceAccountId(),r.targetAccountId()),second=Math.max(r.sourceAccountId(),r.targetAccountId());
  Account one=lock(first),two=lock(second);Account source=one.getId().equals(r.sourceAccountId())?one:two;Account target=one.getId().equals(r.targetAccountId())?one:two;
  active(source);active(target);if(source.getBalance().compareTo(r.amount())<0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient funds");
  BigDecimal sPrev=source.getBalance(),tPrev=target.getBalance();source.setBalance(sPrev.subtract(r.amount()));target.setBalance(tPrev.add(r.amount()));
  accounts.save(source);accounts.save(target);String ref=UUID.randomUUID().toString();
  txs.save(new AccountTransaction(source.getId(),"TRANSFER_DEBIT",r.amount(),sPrev,source.getBalance(),ref));
  txs.save(new AccountTransaction(target.getId(),"TRANSFER_CREDIT",r.amount(),tPrev,target.getBalance(),ref));
  Map<String,Object> result=Map.of("transferId",ref,"sourceAccountId",source.getId(),"targetAccountId",target.getId(),"amount",r.amount(),"sourceBalance",source.getBalance(),"targetBalance",target.getBalance(),"status","SUCCESS");
  String json=mapper.writeValueAsString(result);keys.save(new TransferIdempotency(key,hash,json));
  try{kafka.send("banking.audit",ref,"TRANSFER "+json);}catch(Exception ignored){}
  return ResponseEntity.ok(result);
 }
 @PatchMapping("/{id}/freeze") @Transactional public Account freeze(@PathVariable Long id){Account a=lock(id);if(a.getStatus()==AccountStatus.CLOSED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Closed account cannot be frozen");a.setStatus(AccountStatus.FROZEN);return accounts.save(a);}
 @PatchMapping("/{id}/unfreeze") @Transactional public Account unfreeze(@PathVariable Long id){Account a=lock(id);if(a.getStatus()==AccountStatus.CLOSED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Closed account cannot be activated");a.setStatus(AccountStatus.ACTIVE);return accounts.save(a);}
 @PatchMapping("/{id}/close") @Transactional public Account close(@PathVariable Long id){Account a=lock(id);if(a.getBalance().compareTo(BigDecimal.ZERO)!=0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Account balance must be zero before closing");a.setStatus(AccountStatus.CLOSED);return accounts.save(a);}
 private Account lock(Long id){return accounts.lockById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));}
 private void active(Account a){if(a.getStatus()!=AccountStatus.ACTIVE)throw new ResponseStatusException(HttpStatus.CONFLICT,"Account is not active");}
 private String hash(String s)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}
}
