package com.mybank.bankingapp.service;



import com.mybank.bankingapp.exception.AccountNotFoundException;
import com.mybank.bankingapp.exception.InvalidAccountException;
import com.mybank.bankingapp.model.Account;
import com.mybank.bankingapp.model.ResponseMessage;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

public interface AccountService {
    int createAccount(Account account) throws InvalidAccountException;
    List<Account> getAllAccounts();
    Account getAccount(int id) throws AccountNotFoundException, InvalidAccountException;
    boolean updateAccount(int id, Account account) throws AccountNotFoundException, InvalidAccountException;
    boolean deleteAccount(int id) throws AccountNotFoundException;

    long getAccountsWithBalanceGreaterThan1Lac() throws AccountNotFoundException;

    Map<String, Long> getAccountsByType();

    Map<String, Long> getAccountsByTypeSorted();

    Map<String, Double> getAvgBalanceByType();

    List<Integer> getAccountIdsByExactName(String exactName);


    @Transactional
    ResponseMessage<Account> deposit(int id, double amount) throws InvalidAccountException;

    @Transactional
    ResponseMessage<Account> withdraw(int id, double amount) throws InvalidAccountException;
}