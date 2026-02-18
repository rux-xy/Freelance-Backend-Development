package com.example.demo.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String contractId;
    private String clientId;
    private Double amount;
    private String currency = "LKR";
    private String provider = "dummy_stripe";
    // unpaid | processing | paid | failed | refunded
    private String status;
    private String createdAt;
    private String updatedAt;
    private String receiptUrl;
    private String failureReason;
}