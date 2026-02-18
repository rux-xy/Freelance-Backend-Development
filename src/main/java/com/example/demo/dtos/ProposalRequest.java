package com.example.demo.dtos;

import lombok.Data;

@Data
public class ProposalRequest {
    private String jobId;
    private Double bidAmount;
    private String coverLetter;
    private Integer estimatedDays;
}
