package com.codepulse.notificationservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codepulse.notificationservice.entity.NotificationDelivery;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {

    List<NotificationDelivery> findTop20ByProjectIdOrderBySentAtDesc(UUID projectId);
}
