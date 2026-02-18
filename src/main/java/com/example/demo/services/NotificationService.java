package com.example.demo.services;

import com.example.demo.entities.Notification;
import com.example.demo.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void send(String userId, String type, String title, String message, String relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedId(relatedId);
        notification.setRead(false);
        notification.setCreatedAt(Instant.now().toString());
        notificationRepository.save(notification);
    }
}