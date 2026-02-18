package com.example.demo.repositories;

import com.example.demo.entities.ChatThread;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatThreadRepository extends MongoRepository<ChatThread, String> {
    List<ChatThread> findByClientIdOrFreelancerId(String clientId, String freelancerId);
    Optional<ChatThread> findByJobIdAndClientIdAndFreelancerId(
            String jobId, String clientId, String freelancerId);
}