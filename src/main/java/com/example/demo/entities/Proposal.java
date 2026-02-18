package com.example.demo.entities;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

@Data
@Document(collection = "proposals")
public class Proposal {

    @Id
    private String id;

    private String jobId; // reference to Job.id
    private String freelancerId; // reference to User.id (FREELANCER)
    private String freelancerName;
    private String coverLetter;
    private Double bidAmount;
    private Integer estimatedDays;
    private String status;
    private String submittedAt;
    private String updatedAt;
}