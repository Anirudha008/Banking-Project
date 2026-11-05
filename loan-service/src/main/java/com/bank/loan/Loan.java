package com.bank.loan;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="loans")
public class Loan {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long customerId; @Column(nullable=false) private Long accountId;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal principal;
 @Column(nullable=false,precision=8,scale=4) private BigDecimal annualInterestRate;
 @Column(nullable=false) private Integer termMonths;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal outstanding;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private LoanStatus status;
 @Column(nullable=false) private LocalDateTime createdAt;
 protected Loan(){}
 public Loan(Long c,Long a,BigDecimal p,BigDecimal rate,Integer term){customerId=c;accountId=a;principal=p;annualInterestRate=rate;termMonths=term;outstanding=p;status=LoanStatus.PENDING;createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public Long getCustomerId(){return customerId;} public Long getAccountId(){return accountId;} public BigDecimal getPrincipal(){return principal;}
 public BigDecimal getAnnualInterestRate(){return annualInterestRate;} public Integer getTermMonths(){return termMonths;} public BigDecimal getOutstanding(){return outstanding;} public void setOutstanding(BigDecimal v){outstanding=v;}
 public LoanStatus getStatus(){return status;} public void setStatus(LoanStatus v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
