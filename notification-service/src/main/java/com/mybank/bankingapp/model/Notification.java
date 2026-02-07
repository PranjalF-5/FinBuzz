package com.mybank.bankingapp.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transferId;
    private Long accountId;
    private String message;
    private String status; // SENT, PENDING, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String failureReason;
}
