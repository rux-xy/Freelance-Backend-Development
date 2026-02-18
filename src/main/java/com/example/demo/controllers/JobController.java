package com.example.demo.controllers;

import com.example.demo.entities.Job;
import com.example.demo.entities.User;
import com.example.demo.repositories.JobRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    //Post a job (Client Only)
    @PostMapping
    public Job postJob(@RequestHeader("Authorization") String authHeader, @RequestBody Job jobRequest){
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUserId(token);

        User user = userRepository.findByEmail(email).orElseThrow();
        jobRequest.setClientId(user.getId());
        jobRequest.setCreatedByName(user.getName());   // ADD
        jobRequest.setStatus("open");
        jobRequest.setCreatedAt(Instant.now().toString());
        jobRequest.setUpdatedAt(Instant.now().toString());
        return jobRepository.save(jobRequest);
    }

    //Get all jobs
    @GetMapping
    public List<Job> getAllJobs(){
        return jobRepository.findAll();
    }

    // Get jobs posted by a client
    @GetMapping("/my-jobs")
    public List<Job> getMyJobs(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUserId(token);
        User user = userRepository.findByEmail(email).orElseThrow();
        return jobRepository.findByClientId(user.getId());
    }

    //Get job by id
    @GetMapping("/{id}")
    public Job getJob(@PathVariable String id){
        return jobRepository.findById(id).orElseThrow(()->new RuntimeException("Job not found"));
    }

    //Update job (Client only)
    @PutMapping("/{id}")
    public Job updateJob(@RequestHeader("Authorization") String authHeader,
                         @PathVariable String id,
                         @RequestBody Job jobRequest){
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUserId(token);
        User user = userRepository.findByEmail(email).orElseThrow();
        String clientId = user.getId();

        Job job = jobRepository.findById(id).orElseThrow(()->new RuntimeException("Job not found"));

        if(!Objects.equals(job.getClientId(),clientId)){
            throw new RuntimeException("Not Authorized to edit this job");
        }

        job.setTitle(jobRequest.getTitle());
        job.setDescription(jobRequest.getDescription());
        job.setBudget(jobRequest.getBudget());
        job.setCategory(jobRequest.getCategory());
        //Optionally update timestamp
        job.setCreatedAt(Instant.now().toString());

        return jobRepository.save(job);
    }

    @PatchMapping("/{id}/status")
    public Job updateJobStatus(@PathVariable String id, @RequestParam String status) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(status);
        job.setUpdatedAt(Instant.now().toString());
        return jobRepository.save(job);
    }

    @PatchMapping("/{id}/flag")
    public Job toggleFlag(@PathVariable String id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        job.setFlagged(!job.isFlagged());
        job.setUpdatedAt(Instant.now().toString());
        return jobRepository.save(job);
    }

    //Delete job (Client only)
    @DeleteMapping("/{id}")
    public String deleteJob(@RequestHeader("Authorization") String authHeader,
                            @PathVariable String id){
        String token = authHeader.substring(7);

        String email = jwtUtil.extractUserId(token);
        User user = userRepository.findByEmail(email).orElseThrow();
        String clientId = user.getId();

        Job job = jobRepository.findById(id).orElseThrow(()->new RuntimeException("Job not found"));

        if (!Objects.equals(job.getClientId(),clientId)){
            throw new RuntimeException("Not authorized to delete this job");
        }
        jobRepository.delete(job);
        return "Job deleted successfully";
    }
}
