package com.mybank.bankingapp.repository;

import com.mybank.bankingapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance > ?1")
    long countByBalanceGreaterThan(double balance);

    @Query("SELECT a.type, COUNT(a) FROM Account a GROUP BY a.type")
    List<Object[]> countAccountsByType();

    @Query("SELECT a.type, AVG(a.balance) FROM Account a GROUP BY a.type")
    List<Object[]> getAverageBalanceByType();

    @Query("SELECT a.id FROM Account a WHERE a.name = ?1")
    List<Integer> findIdByName(String name);
}
