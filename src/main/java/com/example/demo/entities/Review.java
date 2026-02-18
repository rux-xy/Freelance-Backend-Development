package com.example.demo.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    private String contractId;
    private String freelancerId;
    private String clientId;
    private String clientName;
    private Integer rating;       // 1 to 5
    private String comment;
    private List<String> tags;    // optional tags like "Fast", "Professional"
    private String createdAt;
}