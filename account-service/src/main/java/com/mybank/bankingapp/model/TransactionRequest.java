package com.mybank.bankingapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionRequest {
    private String type;
    private String description;
    private Double amount;
    private int accountId;
}
