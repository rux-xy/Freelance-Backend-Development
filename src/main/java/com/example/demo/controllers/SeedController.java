package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class SeedController {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final NotificationRepository notificationRepository;

    private String d(int daysAgo) {
        return Instant.now().minus(daysAgo, ChronoUnit.DAYS).toString();
    }

    @PostMapping
    public ResponseEntity<String> seed() {
        // Clear existing data
        userRepository.deleteAll();
        jobRepository.deleteAll();
        proposalRepository.deleteAll();
        contractRepository.deleteAll();
        notificationRepository.deleteAll();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Users
        User admin = new User(); admin.setId("u1"); admin.setName("Admin User");
        admin.setEmail("admin@freelancehub.com"); admin.setPassword(encoder.encode("Admin@123")); admin.setRole("ADMIN");

        User kasun = new User(); kasun.setId("u2"); kasun.setName("Kasun Perera");
        kasun.setEmail("kasun@uni.lk"); kasun.setPassword(encoder.encode("Pass@123")); kasun.setRole("CLIENT");

        User nimali = new User(); nimali.setId("u3"); nimali.setName("Nimali Silva");
        nimali.setEmail("nimali@uni.lk"); nimali.setPassword(encoder.encode("Pass@123")); nimali.setRole("FREELANCER");

        User tharindu = new User(); tharindu.setId("u4"); tharindu.setName("Tharindu Jayasinghe");
        tharindu.setEmail("tharindu@uni.lk"); tharindu.setPassword(encoder.encode("Pass@123")); tharindu.setRole("CLIENT");

        User sachini = new User(); sachini.setId("u5"); sachini.setName("Sachini Fernando");
        sachini.setEmail("sachini@uni.lk"); sachini.setPassword(encoder.encode("Pass@123")); sachini.setRole("FREELANCER");

        User ravindu = new User(); ravindu.setId("u6"); ravindu.setName("Ravindu Bandara");
        ravindu.setEmail("ravindu@uni.lk"); ravindu.setPassword(encoder.encode("Pass@123")); ravindu.setRole("FREELANCER");

        User dilini = new User(); dilini.setId("u7"); dilini.setName("Dilini Wickramasinghe");
        dilini.setEmail("dilini@uni.lk"); dilini.setPassword(encoder.encode("Pass@123")); dilini.setRole("CLIENT");

        User ashan = new User(); ashan.setId("u8"); ashan.setName("Ashan Gunawardena");
        ashan.setEmail("ashan@uni.lk"); ashan.setPassword(encoder.encode("Pass@123")); ashan.setRole("FREELANCER");

        User malini = new User(); malini.setId("u9"); malini.setName("Malini Rajapaksa");
        malini.setEmail("malini@uni.lk"); malini.setPassword(encoder.encode("Pass@123")); malini.setRole("CLIENT");

        User chamara = new User(); chamara.setId("u10"); chamara.setName("Chamara Weerasinghe");
        chamara.setEmail("chamara@uni.lk"); chamara.setPassword(encoder.encode("Pass@123")); chamara.setRole("FREELANCER");

        userRepository.saveAll(List.of(admin, kasun, nimali, tharindu, sachini, ravindu, dilini, ashan, malini, chamara));

        // Jobs
        Job j1 = new Job(); j1.setId("j1"); j1.setTitle("Python ML Assignment Help");
        j1.setDescription("Need help implementing a machine learning classification model using scikit-learn for my AI course assignment.");
        j1.setBudget(5000.0); j1.setCategory("Assignment Help"); j1.setStatus("in_progress");
        j1.setClientId("u2"); j1.setCreatedBy("u2"); j1.setCreatedByName("Kasun Perera"); j1.setCreatedAt(d(20));

        Job j2 = new Job(); j2.setId("j2"); j2.setTitle("Business Presentation Design");
        j2.setDescription("Create a professional 20-slide presentation for my marketing course final project.");
        j2.setBudget(3000.0); j2.setCategory("Design/Slides"); j2.setStatus("open");
        j2.setClientId("u4"); j2.setCreatedBy("u4"); j2.setCreatedByName("Tharindu Jayasinghe"); j2.setCreatedAt(d(18));

        Job j3 = new Job(); j3.setId("j3"); j3.setTitle("React Project Support");
        j3.setDescription("Need a tutor to help me build a CRUD application with React and Node.js.");
        j3.setBudget(8000.0); j3.setCategory("Project Support"); j3.setStatus("open");
        j3.setClientId("u2"); j3.setCreatedBy("u2"); j3.setCreatedByName("Kasun Perera"); j3.setCreatedAt(d(15));

        Job j4 = new Job(); j4.setId("j4"); j4.setTitle("Calculus Tutoring Sessions");
        j4.setDescription("Looking for a tutor for Engineering Mathematics II. Need 5 sessions covering integration and differential equations.");
        j4.setBudget(7500.0); j4.setCategory("Tutoring"); j4.setStatus("open");
        j4.setClientId("u7"); j4.setCreatedBy("u7"); j4.setCreatedByName("Dilini Wickramasinghe"); j4.setCreatedAt(d(12));

        Job j5 = new Job(); j5.setId("j5"); j5.setTitle("Medical Notes Compilation");
        j5.setDescription("Need someone to compile and organize anatomy lecture notes into a structured study guide.");
        j5.setBudget(4000.0); j5.setCategory("Notes"); j5.setStatus("open");
        j5.setClientId("u9"); j5.setCreatedBy("u9"); j5.setCreatedByName("Malini Rajapaksa"); j5.setCreatedAt(d(10));

        jobRepository.saveAll(List.of(j1, j2, j3, j4, j5));

        // Proposals
        Proposal p1 = new Proposal(); p1.setId("p1"); p1.setJobId("j1");
        p1.setFreelancerId("u3"); p1.setFreelancerName("Nimali Silva");
        p1.setCoverLetter("I have extensive experience with scikit-learn and ML classification.");
        p1.setBidAmount(4500.0); p1.setEstimatedDays(3); p1.setStatus("accepted"); p1.setSubmittedAt(d(19));

        Proposal p2 = new Proposal(); p2.setId("p2"); p2.setJobId("j1");
        p2.setFreelancerId("u6"); p2.setFreelancerName("Ravindu Bandara");
        p2.setCoverLetter("I can help with your ML assignment.");
        p2.setBidAmount(5000.0); p2.setEstimatedDays(4); p2.setStatus("rejected"); p2.setSubmittedAt(d(19));

        Proposal p3 = new Proposal(); p3.setId("p3"); p3.setJobId("j2");
        p3.setFreelancerId("u5"); p3.setFreelancerName("Sachini Fernando");
        p3.setCoverLetter("I specialize in professional presentation design.");
        p3.setBidAmount(2800.0); p3.setEstimatedDays(2); p3.setStatus("pending"); p3.setSubmittedAt(d(17));

        Proposal p4 = new Proposal(); p4.setId("p4"); p4.setJobId("j3");
        p4.setFreelancerId("u6"); p4.setFreelancerName("Ravindu Bandara");
        p4.setCoverLetter("React and Node.js are my core skills.");
        p4.setBidAmount(7500.0); p4.setEstimatedDays(5); p4.setStatus("pending"); p4.setSubmittedAt(d(14));

        proposalRepository.saveAll(List.of(p1, p2, p3, p4));

        // Contracts
        Contract c1 = new Contract(); c1.setId("c1"); c1.setJobId("j1"); c1.setProposalId("p1");
        c1.setClientId("u2"); c1.setFreelancerId("u3"); c1.setAgreedPrice(4500.0);
        c1.setStatus("active"); c1.setStartedAt(d(17)); c1.setPaymentStatus("paid");

        contractRepository.save(c1);

        return ResponseEntity.ok("Database seeded successfully");
    }
}