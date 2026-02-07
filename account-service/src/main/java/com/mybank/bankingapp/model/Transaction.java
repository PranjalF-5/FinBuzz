package com.mybank.bankingapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @NotNull(message="Type is required")
    private TransactionType type;

    @NotNull(message = "Description cannot be empty")
    private String description;

    @Positive(message = "Amount must be positive")
    private double amount;

    @Column(name="account_id")
    @NotNull(message = "AccountId cannot be empty")
    private Long accountId;

    private LocalDateTime transactionTimestamp;
}
