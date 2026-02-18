package com.example.demo.controllers;

import com.example.demo.entities.Contract;
import com.example.demo.entities.Review;
import com.example.demo.entities.User;
import com.example.demo.repositories.ContractRepository;
import com.example.demo.repositories.ReviewRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // POST /api/reviews — submit a review after contract completion
    @PostMapping
    public ResponseEntity<Review> submitReview(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {

        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User client = userRepository.findByEmail(email).orElseThrow();

        String contractId = (String) body.get("contractId");
        String freelancerId = (String) body.get("freelancerId");

        // Verify contract exists and the caller is the client on it
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getClientId().equals(client.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // One review per contract
        if (reviewRepository.findByContractId(contractId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .build();
        }

        Review review = new Review();
        review.setContractId(contractId);
        review.setFreelancerId(freelancerId);
        review.setClientId(client.getId());
        review.setClientName(client.getName());
        review.setRating(Integer.parseInt(body.get("rating").toString()));
        review.setComment((String) body.get("comment"));

        // tags is optional
        if (body.containsKey("tags")) {
            review.setTags((List<String>) body.get("tags"));
        }

        review.setCreatedAt(Instant.now().toString());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewRepository.save(review));
    }

    // GET /api/reviews/freelancer/{freelancerId} — get all reviews for a freelancer
    @GetMapping("/freelancer/{freelancerId}")
    public List<Review> getFreelancerReviews(@PathVariable String freelancerId) {
        return reviewRepository.findByFreelancerId(freelancerId);
    }

    // GET /api/reviews/contract/{contractId} — check if a contract already has a review
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<Review> getContractReview(@PathVariable String contractId) {
        return reviewRepository.findByContractId(contractId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}