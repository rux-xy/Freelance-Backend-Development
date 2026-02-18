package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String userId;    // who receives this notification
    private String type;      // proposal_submitted | proposal_accepted | contract_created | etc.
    private String title;
    private String message;
    private String relatedId; // ID of the related job/contract/proposal
    @JsonProperty("isRead") //forces JSON field name to be "isRead"
    private boolean isRead = false;

    private String createdAt;
}