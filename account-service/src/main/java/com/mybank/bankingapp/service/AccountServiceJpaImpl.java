package com.mybank.bankingapp.service;

import com.mybank.bankingapp.client.TransactionClient;
import com.mybank.bankingapp.exception.InvalidAccountException;
import com.blueyonder.bankingapp.model.*;
import com.mybank.bankingapp.model.*;
import com.mybank.bankingapp.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.*;
@Data
@Service
@Slf4j
public class AccountServiceJpaImpl implements AccountService{

    private AccountRepository accountRepository;
    private final KafkaTemplate<String, FundTransferReq> kafkaTemplate;

    private TransactionClient transactionClient;

    public AccountServiceJpaImpl(AccountRepository accountRepository, TransactionClient transactionClient,KafkaTemplate<String, FundTransferReq> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.kafkaTemplate= kafkaTemplate;
        this.transactionClient = transactionClient;
    }
    public int createAccount(Account account) {
        accountRepository.save(account);
        return account.getId();
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccount(int id) throws InvalidAccountException {
        return accountRepository.findById(id)
                .orElseThrow(() -> new InvalidAccountException("Account not found with ID: " + id));
    }

    public boolean updateAccount(int id, Account account) {
        if (accountRepository.existsById(id)) {
            account.setId(id);
            accountRepository.save(account);
            return true;
        }
        return false;
    }

    public boolean deleteAccount(int id) {
        if (accountRepository.existsById(id)) {
            accountRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long getAccountsWithBalanceGreaterThan1Lac() {
        return accountRepository.countByBalanceGreaterThan(100000);
    }

    public Map<String, Long> getAccountsByType() {
        Map<String, Long> accountTypeCounts = new HashMap<>();
        List<Object[]> results = accountRepository.countAccountsByType();
        System.out.println(results);

        for (Object[] result : results) {
            accountTypeCounts.put(result[0].toString(), ((Number) result[1]).longValue());
        }
        return accountTypeCounts;
    }

    public Map<String, Long> getAccountsByTypeSorted() {
        Map<String, Long> accountTypeCounts = getAccountsByType();
        return accountTypeCounts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
    }

    public Map<String, Double> getAvgBalanceByType() {
        Map<String, Double> accountTypeAvgBalance = new HashMap<>();
        List<Object[]> results = accountRepository.getAverageBalanceByType();

        for (Object[] result : results) {
            accountTypeAvgBalance.put(result[0].toString(), (Double) result[1]);
        }
        return accountTypeAvgBalance;
    }

    public List<Integer> getAccountIdsByExactName(String exactName) {
        return accountRepository.findIdByName(exactName);
    }


    @Transactional
    @Override
    public ResponseMessage<Account> deposit(int id, double amount) throws InvalidAccountException {
        Optional<Account> accountOpt = accountRepository.findById(id);

        if (accountOpt.isEmpty()) {
            return ResponseMessage.error("Account not found", null);
        }
        Account account = accountOpt.get();
        if(!account.isActive()){
            return ResponseMessage.error("Deposit failed: due to inactive account",account);
        }

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        // Log transaction using Feign
        try {
            TransactionRequest transactionRequest = new TransactionRequest("CREDIT", "Deposit", amount, id);
            ResponseEntity<ResponseMessage<Transaction>> response = transactionClient.createTransaction(transactionRequest);
            if (response.getStatusCode() != HttpStatus.CREATED || !"success".equals(response.getBody().getStatus())) {
                throw new RuntimeException("Failed to log deposit transaction");
            }
            return ResponseMessage.success("Deposit successful", account);
        } catch (Exception e) {
            account.setBalance(account.getBalance() - amount);
            accountRepository.save(account);
            return ResponseMessage.error("Deposit failed: " + e.getMessage(), null);
        }
    }
    @Transactional
    @Override
    public ResponseMessage<Account> withdraw(int id, double amount) throws InvalidAccountException {
        Optional<Account> accountOpt = accountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return ResponseMessage.error("Account not found", null);
        }

        Account account = accountOpt.get();
        if(!account.isActive()){
            return ResponseMessage.error("withdraw failed: inactive account",account);
        }
        if (account.getBalance() < amount) {
            return ResponseMessage.error("Insufficient funds", null);
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        try {
            TransactionRequest transactionRequest = new TransactionRequest("DEBIT", "Withdrawal", amount, id);
            ResponseEntity<ResponseMessage<Transaction>> response = transactionClient.createTransaction(transactionRequest);
            if (response.getStatusCode() != HttpStatus.CREATED || !"success".equals(response.getBody().getStatus())) {
                throw new RuntimeException("Failed to log withdrawal transaction");
            }
            return ResponseMessage.success("Withdrawal successful", account);
        } catch (Exception e) {
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);
            return ResponseMessage.error("Withdrawal failed: " + e.getMessage(), null);
        }
    }
    @KafkaListener(topics = "deduct-source", groupId = "account-service")
    public void handleDeductSource(@Payload FundTransferReq request, @Header(KafkaHeaders.RECEIVED_KEY) String transferId) throws InvalidAccountException {
        request.setId(transferId);
        Optional<Account> sourceAccount = accountRepository.findById(request.getSourceAccountId());


        if (sourceAccount.isPresent() && sourceAccount.get().getBalance() >= request.getAmount()&&sourceAccount.get().isActive()) {
            sourceAccount.get().setBalance(sourceAccount.get().getBalance() - request.getAmount());
            accountRepository.save(sourceAccount.get());
            kafkaTemplate.send("source-deducted", transferId, request);
        } else {
            if(!sourceAccount.get().isActive()){
                request.setFailureReason( "Source account not active please activate it before any transferring funds");
            }
            else
                request.setFailureReason(sourceAccount.isPresent() ? "Insufficient funds in source account" : "Source account not found");
            request.setStatus("deduction-failed");
            kafkaTemplate.send("deduction-failed", transferId, request);
        }
    }

    //fund-transfer - amount add
    @KafkaListener(topics = "credit-target", groupId = "account-service")
    public void handleCreditTarget(@Payload FundTransferReq request, @Header(KafkaHeaders.RECEIVED_KEY) String transferId) throws InvalidAccountException {
        Optional<Account> targetAccount = accountRepository.findById(request.getTargetAccountId());

        if (targetAccount.isPresent()&&targetAccount.get().isActive()) {
            targetAccount.get().setBalance(targetAccount.get().getBalance() + request.getAmount());
            accountRepository.save(targetAccount.get());
            kafkaTemplate.send("target-credited", transferId, request);
        } else {
            if(!targetAccount.get().isActive()){
                request.setFailureReason( "Target account not active please activate it before any transferring funds");
            }
            else
                request.setFailureReason("Target account not found");
            request.setStatus("crediting-failed");
            kafkaTemplate.send("crediting-failed", transferId, request);
        }
    }

    // If credit failed then refund the source
    @KafkaListener(topics = "refund-source", groupId = "account-service")
    public void handleRefundSource(@Payload FundTransferReq request, @Header(KafkaHeaders.RECEIVED_KEY) String transferId) {
        Optional<Account> sourceAccount = accountRepository.findById(request.getSourceAccountId());
        sourceAccount.ifPresent(account -> {
            try {
                account.setBalance(account.getBalance() + request.getAmount());
            } catch (InvalidAccountException e) {
                throw new RuntimeException(e);
            }
            accountRepository.save(account);
        });
    }

    // if logging failed then refund the source and deduct the amount from target
    @KafkaListener(topics = "decrement-target", groupId = "account-service")
    public void handleDecrementTarget(@Payload FundTransferReq request, @Header(KafkaHeaders.RECEIVED_KEY) String transferId) {
        Optional<Account> targetAccount = accountRepository.findById(request.getTargetAccountId());
        targetAccount.ifPresent(account -> {
            try {
                account.setBalance(account.getBalance() - request.getAmount());
            } catch (InvalidAccountException e) {
                throw new RuntimeException(e);
            }
            accountRepository.save(account);
        });
    }
}
