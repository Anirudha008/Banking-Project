package com.bank.customer;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="customers")
public class Customer {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=60) private String firstName;
 @Column(nullable=false,length=60) private String lastName;
 @Column(nullable=false) private LocalDate dateOfBirth;
 @Column(nullable=false,unique=true,length=150) private String email;
 @Column(nullable=false,unique=true,length=20) private String phoneNumber;
 @Column(nullable=false,length=300) private String address;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private KycStatus kycStatus=KycStatus.PENDING;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private CustomerStatus status=CustomerStatus.ACTIVE;
 @Column(nullable=false,updatable=false) private LocalDateTime createdAt;
 @Column(nullable=false) private LocalDateTime updatedAt;
 @PrePersist void create(){createdAt=updatedAt=LocalDateTime.now();}
 @PreUpdate void update(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getFirstName(){return firstName;} public void setFirstName(String v){firstName=v;}
 public String getLastName(){return lastName;} public void setLastName(String v){lastName=v;}
 public LocalDate getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(LocalDate v){dateOfBirth=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getPhoneNumber(){return phoneNumber;} public void setPhoneNumber(String v){phoneNumber=v;}
 public String getAddress(){return address;} public void setAddress(String v){address=v;}
 public KycStatus getKycStatus(){return kycStatus;} public void setKycStatus(KycStatus v){kycStatus=v;}
 public CustomerStatus getStatus(){return status;} public void setStatus(CustomerStatus v){status=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
