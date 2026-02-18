package com.example.demo.controllers;

import com.example.demo.entities.Notification;
import com.example.demo.entities.User;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // GET /api/notifications — get all notifications for logged-in user
    @GetMapping
    public List<Notification> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    // PATCH /api/notifications/{id}/read — mark one as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return ResponseEntity.ok(notificationRepository.save(notification));
    }

    // POST /api/notifications/read-all — mark all as read for logged-in user
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Notification> all = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        all.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(all);

        return ResponseEntity.ok().build();
    }
}