package com.bank.account;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="transfer_idempotency")
public class TransferIdempotency {
 @Id @Column(length=100) private String idempotencyKey;
 @Column(nullable=false,length=64) private String requestHash;
 @Column(nullable=false,length=4000) private String responseJson;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 protected TransferIdempotency(){}
 public TransferIdempotency(String k,String h,String j){idempotencyKey=k;requestHash=h;responseJson=j;}
 public String getRequestHash(){return requestHash;} public String getResponseJson(){return responseJson;}
}
