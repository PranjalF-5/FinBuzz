package com.mybank.bankingapp.repository;

import com.mybank.bankingapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
//    @Query("SELECT * FROM Transaction a WHERE a.accountId =?1")
//    List<Transaction> findByAccountId(int accountId);
@Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId")
List<Transaction> findByAccountId(@Param("accountId") Long accountId);

}
