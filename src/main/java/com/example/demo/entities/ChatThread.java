package com.example.demo.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "chat_threads")
public class ChatThread {

    @Id
    private String id;

    private String jobId;
    private String clientId;
    private String freelancerId;
    private String createdAt;
    private String lastMessageAt;
}