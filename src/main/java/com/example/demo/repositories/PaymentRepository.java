package com.example.demo.repositories;

import com.example.demo.entities.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByContractId(String contractId);
    List<Payment> findByClientId(String clientId);
}