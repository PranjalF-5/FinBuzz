package com.mybank.bankingapp.model;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WithdrawalRequest {
    @Positive(message="Amount must be positive")
    private Double amount;
}
