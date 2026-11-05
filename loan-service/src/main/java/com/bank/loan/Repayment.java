package com.bank.loan;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="repayments")
public class Repayment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long loanId; @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(nullable=false) private LocalDateTime paidAt;
 protected Repayment(){}
 public Repayment(Long loanId,BigDecimal amount){this.loanId=loanId;this.amount=amount;paidAt=LocalDateTime.now();}
 public Long getId(){return id;} public Long getLoanId(){return loanId;} public BigDecimal getAmount(){return amount;} public LocalDateTime getPaidAt(){return paidAt;}
}
