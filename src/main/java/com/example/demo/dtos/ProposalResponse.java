package com.example.demo.dtos;

import lombok.Data;

@Data
public class ProposalResponse {
    private String id;
    private String jobId;
    private String freelancerId;
    private String freelancerName;
    private String coverLetter;
    private Double bidAmount;
    private Integer estimatedDays;
    private String status;
    private String submittedAt;
    private String updatedAt;
}
