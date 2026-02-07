package com.mybank.bankingapp.client;

import com.mybank.bankingapp.model.ResponseMessage;
import com.mybank.bankingapp.model.Transaction;
import com.mybank.bankingapp.model.TransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "transaction-service")
public interface TransactionClient {
    @PostMapping("/transactions")
    ResponseEntity<ResponseMessage<Transaction>> createTransaction(@RequestBody TransactionRequest transactionRequest);
}
