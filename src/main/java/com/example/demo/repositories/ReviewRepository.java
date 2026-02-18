package com.example.demo.repositories;

import com.example.demo.entities.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByFreelancerId(String freelancerId);
    Optional<Review> findByContractId(String contractId);
}