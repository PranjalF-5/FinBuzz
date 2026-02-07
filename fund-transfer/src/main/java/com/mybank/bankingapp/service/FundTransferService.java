package com.mybank.bankingapp.service;

import com.mybank.bankingapp.model.FundTransferReq;
import com.mybank.bankingapp.repository.FundTransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FundTransferService {
    private final KafkaTemplate<String, FundTransferReq> kafkaTemplate;
    private final FundTransferRepository fundTransferRepository;

    public FundTransferService(KafkaTemplate<String, FundTransferReq> kafkaTemplate, FundTransferRepository fundTransferRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.fundTransferRepository = fundTransferRepository;
    }

    public FundTransferReq initiateFundTransfer(FundTransferReq fundTransferRequest) {
        fundTransferRequest.setId(UUID.randomUUID().toString());
        fundTransferRequest.setStatus("INITIATED");
        fundTransferRequest.setCreatedAt(LocalDateTime.now());
        fundTransferRequest.setUpdatedAt(LocalDateTime.now());
        fundTransferRepository.save(fundTransferRequest);
        fundTransferRequest.setStatus("DEDUCTING");
        fundTransferRepository.save(fundTransferRequest);
        kafkaTemplate.send("deduct-source", fundTransferRequest.getId(), fundTransferRequest);
        return fundTransferRequest;
    }

    public Optional<FundTransferReq> getTransferRequest(String transferId) {
        return fundTransferRepository.findById(transferId);
    }

    @KafkaListener(topics="source-deducted", groupId = "fund-transfer-service")
    public void handleSourceDeducted(FundTransferReq fundTransferRequest) {
        FundTransferReq transferRequest = fundTransferRepository.findById(fundTransferRequest.getId()).orElse(null);
        if (transferRequest != null) {
            transferRequest.setStatus("CREDITING");
            transferRequest.setUpdatedAt(LocalDateTime.now());
            fundTransferRepository.save(transferRequest);
            kafkaTemplate.send("credit-target", fundTransferRequest.getId(), fundTransferRequest);
        }
    }

    @KafkaListener(topics = "target-credited", groupId = "fund-transfer-service")
    public void handleTargetCredited(FundTransferReq fundTransferRequest) {
        FundTransferReq transfer = fundTransferRepository.findById(fundTransferRequest.getId()).orElse(null);
        if (transfer != null) {
            transfer.setStatus("LOGGING");
            transfer.setUpdatedAt(LocalDateTime.now());
            fundTransferRepository.save(transfer);
            kafkaTemplate.send("log-transactions", fundTransferRequest.getId(), fundTransferRequest);
        }
    }

    @KafkaListener(topics = "transactions-logged", groupId = "fund-transfer-service")
    public void handleTransactionsLogged(FundTransferReq fundTransferRequest) {
        FundTransferReq transfer = fundTransferRepository.findById(fundTransferRequest.getId()).orElse(null);

        if (transfer != null) {
            transfer.setStatus("COMPLETED");
            transfer.setUpdatedAt(LocalDateTime.now());
            fundTransferRepository.save(transfer);
            kafkaTemplate.send("notify-users", transfer.getId(), transfer); // Notification is post-success
        }
    }

    @KafkaListener(topics = {"deduction-failed", "crediting-failed", "logging-failed"}, groupId = "fund-transfer-service")
    public void handleFailure(FundTransferReq fundTransferRequest) {
        FundTransferReq transfer = fundTransferRepository.findById(fundTransferRequest.getId()).orElse(null);
        if (transfer != null) {
            transfer.setStatus("FAILED");
            transfer.setFailureReason(fundTransferRequest.getFailureReason());
            transfer.setUpdatedAt(LocalDateTime.now());
            fundTransferRepository.save(transfer);
            if ("deduction-failed".equals(fundTransferRequest.getStatus())) {}
            else if ("crediting-failed".equals(fundTransferRequest.getStatus())) {
                kafkaTemplate.send("refund-source", fundTransferRequest.getId(), fundTransferRequest);
            } else if ("logging-failed".equals(fundTransferRequest.getStatus())) {
                kafkaTemplate.send("refund-source", fundTransferRequest.getId(), fundTransferRequest);
                kafkaTemplate.send("decrement-target", fundTransferRequest.getId(), fundTransferRequest);
            }
            kafkaTemplate.send("notify-users",transfer.getId(), transfer);

        }
    }

}
