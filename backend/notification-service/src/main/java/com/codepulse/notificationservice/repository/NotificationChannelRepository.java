package com.codepulse.notificationservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codepulse.notificationservice.entity.NotificationChannel;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, UUID> {

    List<NotificationChannel> findByProjectIdAndActiveTrue(UUID projectId);
}
