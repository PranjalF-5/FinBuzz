package com.mybank.bankingapp.service;

import com.mybank.bankingapp.model.FundTransferReq;
import com.mybank.bankingapp.model.Transaction;
import com.mybank.bankingapp.model.TransactionType;
import com.mybank.bankingapp.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, FundTransferReq> kafkaTemplate;

    public TransactionService(TransactionRepository transactionRepository, KafkaTemplate<String, FundTransferReq> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
//        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Transaction> getAllTransactions(){

        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id){
        return transactionRepository.findById(id);
    }

    public List<Transaction> findByAccountId(Long account_id)
    {
        return transactionRepository.findByAccountId(account_id);
    }


    public Transaction createTransaction(Transaction transaction){
        transaction.setTransactionTimestamp(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    @KafkaListener(topics = "log-transactions", groupId = "transaction-service")
    @Transactional
    public void handleLogTransactions(@Payload FundTransferReq request, @Header(KafkaHeaders.RECEIVED_KEY) String transferId) {

        try {
            Transaction debit = new Transaction();
            debit.setType(TransactionType.DEBIT);
            debit.setDescription(request.getDescription());
            debit.setAmount(request.getAmount());
            debit.setAccountId(request.getSourceAccountId());
            debit.setTransactionTimestamp(LocalDateTime.now());
            transactionRepository.save(debit);

            Transaction credit = new Transaction();
            credit.setType(TransactionType.CREDIT);
            credit.setDescription(request.getDescription());
            credit.setAmount(request.getAmount());
            credit.setAccountId(request.getTargetAccountId());
            credit.setTransactionTimestamp(LocalDateTime.now());
            transactionRepository.save(credit);

            kafkaTemplate.send("transactions-logged", transferId, request);
        } catch (Exception e) {
            request.setFailureReason("Transaction logging failed: " + e.getMessage());
            request.setStatus("logging-failed");
            kafkaTemplate.send("logging-failed", transferId, request);
            throw e;
        }
    }




}
