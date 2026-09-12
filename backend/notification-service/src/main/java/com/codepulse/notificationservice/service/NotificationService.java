package com.codepulse.notificationservice.service;

import java.util.List;
import java.util.UUID;

import com.codepulse.notificationservice.dto.NotificationRequest;
import com.codepulse.notificationservice.dto.NotificationResponse;
import com.codepulse.notificationservice.entity.NotificationDelivery;

public interface NotificationService {

    NotificationResponse sendNotifications(NotificationRequest request);

    List<NotificationDelivery> getDeliveryHistory(UUID projectId);
}
