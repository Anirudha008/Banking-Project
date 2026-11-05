package com.bank.account;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="accounts")
public class Account {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long customerId;
 @Column(nullable=false,unique=true,length=24) private String accountNumber;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private AccountType accountType;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal balance;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private AccountStatus status=AccountStatus.ACTIVE;
 @Version private Long version;
 @Column(nullable=false) private LocalDateTime createdAt;
 @PrePersist void create(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;}
 public String getAccountNumber(){return accountNumber;} public void setAccountNumber(String v){accountNumber=v;}
 public AccountType getAccountType(){return accountType;} public void setAccountType(AccountType v){accountType=v;}
 public BigDecimal getBalance(){return balance;} public void setBalance(BigDecimal v){balance=v;}
 public AccountStatus getStatus(){return status;} public void setStatus(AccountStatus v){status=v;}
}
