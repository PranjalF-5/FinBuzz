package com.mybank.bankingapp.service;

import com.mybank.bankingapp.model.FundTransferReq;
import com.mybank.bankingapp.model.Notification;
import com.mybank.bankingapp.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics="notify-users", groupId = "notification-service")
    public void handleNotifyUser(FundTransferReq request, @Header(KafkaHeaders.RECEIVED_KEY) String transferId) {
        log.info("Received fund transfer request: " + request);
        if("COMPLETED".equals(request.getStatus())) {
            //For source account
            Notification sourceNotification = new Notification();
            sourceNotification.setTransferId(request.getId());
            sourceNotification.setAccountId(request.getSourceAccountId());
            sourceNotification.setMessage("Transfer of " + request.getAmount() + " to account " + request.getTargetAccountId() + " completed successfully");
            sourceNotification.setStatus("PENDING");
            sourceNotification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(sourceNotification);

            //For target account
            Notification targetNotification = new Notification();
            targetNotification.setTransferId(request.getId());
            targetNotification.setAccountId(request.getTargetAccountId());
            targetNotification.setMessage("Received " + request.getAmount() + " from account " + request.getSourceAccountId());
            targetNotification.setStatus("PENDING");
            targetNotification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(targetNotification);

            sendNotification(sourceNotification);
            sendNotification(targetNotification);

        } else if("FAILED".equals(request.getStatus())) {
            //Notifying only source account only
            Notification sourceNotification = new Notification();
            sourceNotification.setTransferId(request.getId());
            sourceNotification.setAccountId(request.getSourceAccountId());
            sourceNotification.setMessage("Transfer of " + request.getAmount() + " failed: " + request.getFailureReason());
            sourceNotification.setStatus("PENDING");
            sourceNotification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(sourceNotification);

            sendNotification(sourceNotification);
        }
    }

    private void sendNotification(Notification notification) {
        try{
            System.out.println("Sending notification to user " + notification.getAccountId() + " with status " + notification.getStatus());

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());

            notificationRepository.save(notification);
        }catch (Exception e){
            notification.setStatus("FAILED");
            notification.setFailureReason("Notification failed: " + e.getMessage());
            notificationRepository.save(notification);
        }
    }
}
