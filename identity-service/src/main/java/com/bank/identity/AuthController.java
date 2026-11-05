package com.bank.identity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final UserRepository users;
 private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
 @Value("${app.jwt.secret:local-poc-secret-key-change-this-32bytes-minimum}") private String secret;
 public AuthController(UserRepository users){this.users=users;}
 public record RegisterRequest(@NotBlank String username,@NotBlank String password,String role){}
 public record LoginRequest(@NotBlank String username,@NotBlank String password){}
 @PostMapping("/register")
 public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r){
  if(r.password().length()<10) return ResponseEntity.badRequest().body(Map.of("message","Password must contain at least 10 characters"));
  if(users.existsByUsername(r.username())) return ResponseEntity.status(409).body(Map.of("message","Username already exists"));
  String role=(r.role()==null||r.role().isBlank())?"CUSTOMER":r.role().toUpperCase();
  if(!java.util.Set.of("CUSTOMER","TELLER","LOAN_OFFICER","ADMIN").contains(role)) return ResponseEntity.badRequest().body(Map.of("message","Invalid role"));
  users.save(new UserAccount(r.username(),encoder.encode(r.password()),role));
  return ResponseEntity.status(201).body(Map.of("message","User registered"));
 }
 @PostMapping("/login")
 public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r){
  var user=users.findByUsername(r.username()).orElse(null);
  if(user==null||!encoder.matches(r.password(),user.getPassword())) return ResponseEntity.status(401).body(Map.of("message","Invalid username or password"));
  Instant now=Instant.now();
  String token=Jwts.builder().subject(user.getUsername()).claim("role",user.getRole()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(3600)))
   .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
  return ResponseEntity.ok(Map.of("accessToken",token,"tokenType","Bearer","expiresInSeconds",3600,"role",user.getRole()));
 }
}
