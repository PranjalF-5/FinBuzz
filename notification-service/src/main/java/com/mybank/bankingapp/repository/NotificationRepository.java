package com.mybank.bankingapp.repository;

import com.mybank.bankingapp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
