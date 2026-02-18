package com.example.demo.entities;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    @Indexed
    private String clientId; // reference to User.id (CLIENT)
    private String createdByName;
    private String title;
    private String description;
    private Double budget;
    private String category;
    private List<String> skills = new ArrayList<>();
    private String status = "open";
    private boolean flagged = false;
    private String createdAt;
    private String updatedAt;
}