package com.retailpos.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findByOrderByCreatedAtDesc();
    long countByIsReadFalse();
}
