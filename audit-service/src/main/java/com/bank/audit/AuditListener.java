package com.bank.audit;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component
public class AuditListener {
 private final AuditRepository repository;
 public AuditListener(AuditRepository repository){this.repository=repository;}
 @KafkaListener(topics="banking.audit",groupId="audit-service")
 public void receive(String payload){
  String key=Integer.toHexString(payload.hashCode());
  if(repository.findByEventKey(key).isEmpty())repository.save(new AuditRecord(key,"BANKING_EVENT",payload));
 }
}
