package com.example.demo.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    private String threadId;
    private String senderId;
    private String text;
    private String createdAt;
    private List<String> readBy = new ArrayList<>();
}