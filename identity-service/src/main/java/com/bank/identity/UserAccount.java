package com.bank.identity;
import jakarta.persistence.*;
@Entity @Table(name="users")
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=80) private String username;
 @Column(nullable=false) private String password;
 @Column(nullable=false,length=30) private String role;
 protected UserAccount(){}
 public UserAccount(String u,String p,String r){username=u;password=p;role=r;}
 public Long getId(){return id;} public String getUsername(){return username;}
 public String getPassword(){return password;} public String getRole(){return role;}
}
