package com.example.demo.controllers;

import com.example.demo.entities.ChatThread;
import com.example.demo.entities.Message;
import com.example.demo.entities.User;
import com.example.demo.repositories.ChatThreadRepository;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatThreadRepository chatThreadRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // GET /api/chat/threads — get all threads for logged-in user
    @GetMapping("/threads")
    public List<ChatThread> getMyThreads(
            @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow();
        // Returns threads where user is either the client or freelancer
        return chatThreadRepository
                .findByClientIdOrFreelancerId(user.getId(), user.getId());
    }

    // POST /api/chat/threads — create or return existing thread
    @PostMapping("/threads")
    public ResponseEntity<ChatThread> createThread(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String jobId = body.get("jobId");
        String clientId = body.get("clientId");
        String freelancerId = body.get("freelancerId");

        // Return existing thread if one already exists for this job + pair
        return chatThreadRepository
                .findByJobIdAndClientIdAndFreelancerId(jobId, clientId, freelancerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    String now = Instant.now().toString();
                    ChatThread thread = new ChatThread();
                    thread.setJobId(jobId);
                    thread.setClientId(clientId);
                    thread.setFreelancerId(freelancerId);
                    thread.setCreatedAt(now);
                    thread.setLastMessageAt(now);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(chatThreadRepository.save(thread));
                });
    }

    // GET /api/chat/threads/{id} — get a single thread by ID
    @GetMapping("/threads/{id}")
    public ResponseEntity<ChatThread> getThread(@PathVariable String id) {
        return chatThreadRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/chat/threads/{id}/messages — get all messages in a thread
    @GetMapping("/threads/{id}/messages")
    public List<Message> getMessages(@PathVariable String id) {
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(id);
    }

    // POST /api/chat/threads/{id}/messages — send a message
    @PostMapping("/threads/{id}/messages")
    public ResponseEntity<Message> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User sender = userRepository.findByEmail(email).orElseThrow();

        ChatThread thread = chatThreadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        String now = Instant.now().toString();

        Message message = new Message();
        message.setThreadId(id);
        message.setSenderId(sender.getId());
        message.setText(body.get("text"));
        message.setCreatedAt(now);
        message.getReadBy().add(sender.getId()); // sender has already "read" it

        messageRepository.save(message);

        // Update lastMessageAt on the thread
        thread.setLastMessageAt(now);
        chatThreadRepository.save(thread);

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    // POST /api/chat/threads/{id}/read — mark all messages in thread as read
    @PostMapping("/threads/{id}/read")
    public ResponseEntity<Void> markThreadRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Message> messages = messageRepository
                .findByThreadIdOrderByCreatedAtAsc(id);

        messages.forEach(m -> {
            if (!m.getReadBy().contains(user.getId())) {
                m.getReadBy().add(user.getId());
            }
        });
        messageRepository.saveAll(messages);

        return ResponseEntity.ok().build();
    }

    // GET /api/chat/unread-count — total unread messages across all threads
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {

        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow();

        List<ChatThread> threads = chatThreadRepository
                .findByClientIdOrFreelancerId(user.getId(), user.getId());

        int count = 0;
        for (ChatThread thread : threads) {
            List<Message> unread = messageRepository
                    .findByThreadIdAndSenderIdNot(thread.getId(), user.getId());
            count += unread.stream()
                    .filter(m -> !m.getReadBy().contains(user.getId()))
                    .count();
        }

        return ResponseEntity.ok(Map.of("count", count));
    }
}