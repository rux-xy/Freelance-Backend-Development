package com.example.demo.repositories;

import com.example.demo.entities.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByThreadIdOrderByCreatedAtAsc(String threadId);
    List<Message> findByThreadIdAndSenderIdNot(String threadId, String senderId);
}