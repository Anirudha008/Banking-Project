package com.bank.audit;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="audit_records")
public class AuditRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=80) private String eventKey;
 @Column(nullable=false,length=100) private String eventType;
 @Column(nullable=false,length=4000) private String payload;
 @Column(nullable=false) private LocalDateTime receivedAt;
 protected AuditRecord(){}
 public AuditRecord(String k,String t,String p){eventKey=k;eventType=t;payload=p;receivedAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getEventKey(){return eventKey;} public String getEventType(){return eventType;} public String getPayload(){return payload;} public LocalDateTime getReceivedAt(){return receivedAt;}
}
