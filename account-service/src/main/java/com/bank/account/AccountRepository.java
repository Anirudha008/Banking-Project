package com.bank.account;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface AccountRepository extends JpaRepository<Account,Long>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Account a where a.id=:id")
 Optional<Account> lockById(@Param("id") Long id);
 List<Account> findByCustomerId(Long customerId);
}
