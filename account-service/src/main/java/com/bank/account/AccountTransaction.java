package com.bank.account;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="account_transactions")
public class AccountTransaction {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long accountId;
 @Column(nullable=false,length=30) private String transactionType;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal previousBalance;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal newBalance;
 @Column(nullable=false,length=80) private String reference;
 @Column(nullable=false) private LocalDateTime createdAt;
 protected AccountTransaction(){}
 public AccountTransaction(Long a,String t,BigDecimal amount,BigDecimal prev,BigDecimal next,String ref){accountId=a;transactionType=t;this.amount=amount;previousBalance=prev;newBalance=next;reference=ref;createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public Long getAccountId(){return accountId;} public String getTransactionType(){return transactionType;}
 public BigDecimal getAmount(){return amount;} public BigDecimal getPreviousBalance(){return previousBalance;} public BigDecimal getNewBalance(){return newBalance;}
 public String getReference(){return reference;} public LocalDateTime getCreatedAt(){return createdAt;}
}
