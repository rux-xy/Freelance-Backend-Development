package com.example.demo.controllers;

import com.example.demo.dtos.ProposalRequest;
import com.example.demo.dtos.ProposalResponse;
import com.example.demo.entities.Contract;
import com.example.demo.entities.Job;
import com.example.demo.entities.Proposal;
import com.example.demo.entities.User;
import com.example.demo.repositories.ContractRepository;
import com.example.demo.repositories.JobRepository;
import com.example.demo.repositories.ProposalRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController {
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    private ProposalResponse mapToResponse(Proposal proposal) {
        ProposalResponse response = new ProposalResponse();
        response.setId(proposal.getId());
        response.setJobId(proposal.getJobId());
        response.setFreelancerId(proposal.getFreelancerId());
        response.setFreelancerName(proposal.getFreelancerName());
        response.setBidAmount(proposal.getBidAmount());
        response.setEstimatedDays(proposal.getEstimatedDays());
        response.setCoverLetter(proposal.getCoverLetter());
        response.setStatus(proposal.getStatus());
        response.setSubmittedAt(proposal.getSubmittedAt());
        response.setUpdatedAt(proposal.getUpdatedAt());
        return response;
    }

    @PostMapping
    public ProposalResponse submitProposal(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody ProposalRequest request) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUserId(token);
        User freelancer = userRepository.findByEmail(email).orElseThrow();

        // fetch the job so we have the clientId for the notification
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Proposal proposal = new Proposal();
        proposal.setJobId(request.getJobId());
        proposal.setCoverLetter(request.getCoverLetter());
        proposal.setFreelancerId(freelancer.getId());
        proposal.setFreelancerName(freelancer.getName());
        proposal.setBidAmount(request.getBidAmount());
        proposal.setEstimatedDays(request.getEstimatedDays());
        proposal.setStatus("pending");
        proposal.setSubmittedAt(Instant.now().toString());
        proposal.setUpdatedAt(Instant.now().toString());

        // save FIRST so proposal.getId() is populated, then notify
        Proposal savedProposal = proposalRepository.save(proposal);

        notificationService.send(
                job.getClientId(),
                "proposal_submitted",
                "New Proposal Received",
                freelancer.getName() + " submitted a proposal for your job",
                job.getId()
        );

        return mapToResponse(savedProposal);
    }

    @GetMapping("/job/{jobId}")
    public List<ProposalResponse> getProposalsForJob(@PathVariable String jobId) {
        return proposalRepository.findByJobId(jobId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/my-proposals")
    public List<ProposalResponse> getMyProposals(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUserId(token);
        User freelancer = userRepository.findByEmail(email).orElseThrow();
        return proposalRepository.findByFreelancerId(freelancer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/status")
    public ProposalResponse updateProposalStatus(@PathVariable String id,
                                                 @RequestParam String status) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));
        if (!status.equals("accepted") && !status.equals("rejected") && !status.equals("withdrawn")) {
            throw new RuntimeException("Invalid status");
        }
        proposal.setStatus(status);
        proposal.setUpdatedAt(Instant.now().toString());
        return mapToResponse(proposalRepository.save(proposal));
    }

    @PostMapping("/{proposalId}/accept")
    public Contract acceptProposal(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable String proposalId) {
        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User caller = userRepository.findByEmail(email).orElseThrow();

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        Job job = jobRepository.findById(proposal.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getClientId().equals(caller.getId())) {
            throw new RuntimeException("Only the job owner can accept proposals");
        }

        if ("accepted".equals(proposal.getStatus())) {
            throw new RuntimeException("Proposal already accepted");
        }

        proposal.setStatus("accepted");
        proposal.setUpdatedAt(Instant.now().toString());
        proposalRepository.save(proposal);

        if (contractRepository.existsByProposalId(proposalId)) {
            throw new RuntimeException("Contract already exists");
        }

        Contract contract = new Contract();
        contract.setJobId(job.getId());
        contract.setClientId(job.getClientId());
        contract.setFreelancerId(proposal.getFreelancerId());
        contract.setProposalId(proposal.getId());
        contract.setAgreedPrice(proposal.getBidAmount());
        contract.setStatus("active");
        contract.setStartedAt(Instant.now().toString());
        contract.setPaymentStatus("unpaid");

        // save FIRST so contract.getId() is populated, then notify

        Contract savedContract = contractRepository.save(contract);

        notificationService.send(
                proposal.getFreelancerId(),
                "proposal_accepted",
                "Your Proposal Was Accepted",
                "Your proposal has been accepted. A contract has been created.",
                savedContract.getId()
        );

        return savedContract;
    }
}