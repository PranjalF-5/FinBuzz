package com.mybank.bankingapp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="transactions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
