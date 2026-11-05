package com.bank.identity;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
 @Bean BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
  return http.csrf(c->c.disable()).authorizeHttpRequests(a->a.anyRequest().permitAll()).build();
 }
}
