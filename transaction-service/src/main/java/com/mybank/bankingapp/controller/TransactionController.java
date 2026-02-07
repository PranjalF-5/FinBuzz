package com.mybank.bankingapp.controller;

import com.mybank.bankingapp.model.ResponseMessage;
import com.mybank.bankingapp.model.Transaction;
import com.mybank.bankingapp.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ResponseMessage<List<Transaction>>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        String message = transactions.isEmpty() ? "No transactions found" : "Found " + transactions.size() + " transactions";
        return ResponseEntity.ok(ResponseMessage.success(message, transactions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<Transaction>> getTransactionById(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.getTransactionById(id);
        return transaction
                .map(t-> ResponseEntity.ok(ResponseMessage.success("Transaction retrieved successfully",t)))
                .orElseGet(()-> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseMessage.error("Transaction with ID "+id+" not found",null)));
    }
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(@PathVariable Long accountId) {
        List<Transaction> transactions = transactionService.findByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }
    @PostMapping
    public ResponseEntity<ResponseMessage<Transaction>> createTransaction(@Valid @RequestBody Transaction transaction) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseMessage.success("Transaction created successfully",createdTransaction));
    }
}
