package com.mybank.bankingapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="fund_transfer")
public class FundTransferReq {
    @Id
    private String id;
    private long sourceAccountId;
    private long targetAccountId;

    @Positive(message="Amount must be positive")
    private double amount;
    private String description;
    private String status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
