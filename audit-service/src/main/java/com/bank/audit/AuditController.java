package com.bank.audit;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/audit")
public class AuditController {
 private final AuditRepository repo;
 public AuditController(AuditRepository repo){this.repo=repo;}
 @GetMapping public Page<AuditRecord> all(@PageableDefault(size=20,sort="receivedAt") Pageable p){return repo.findAll(p);}
 @GetMapping("/{id}") public AuditRecord get(@PathVariable Long id){return repo.findById(id).orElseThrow();}
}
