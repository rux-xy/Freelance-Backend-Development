package com.example.demo.controllers;

import com.example.demo.entities.Contract;
import com.example.demo.entities.Payment;
import com.example.demo.entities.User;
import com.example.demo.repositories.ContractRepository;
import com.example.demo.repositories.PaymentRepository;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // POST /api/payments — create a payment for a contract
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {

        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User client = userRepository.findByEmail(email).orElseThrow();

        String contractId = (String) body.get("contractId");
        Double amount = Double.parseDouble(body.get("amount").toString());

        // Prevent duplicate payments for the same contract
        if (paymentRepository.findByContractId(contractId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String now = Instant.now().toString();
        Payment payment = new Payment();
        payment.setContractId(contractId);
        payment.setClientId(client.getId());
        payment.setAmount(amount);
        payment.setStatus("processing");
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentRepository.save(payment));
    }

    // GET /api/payments/{id} — get one payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return ResponseEntity.ok(payment);
    }

    // GET /api/payments/contract/{contractId} — get payment by contract
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<Payment> getPaymentByContract(@PathVariable String contractId) {
        Payment payment = paymentRepository.findByContractId(contractId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return ResponseEntity.ok(payment);
    }

    // GET /api/payments/my — list all payments for the logged-in client
    @GetMapping("/my")
    public List<Payment> getMyPayments(@RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractUserId(authHeader.substring(7));
        User client = userRepository.findByEmail(email).orElseThrow();
        return paymentRepository.findByClientId(client.getId());
    }

    // PATCH /api/payments/{id}/status — manually update payment status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Payment> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        String status = body.get("status");
        String failureReason = body.get("failureReason");

        payment.setStatus(status);
        payment.setUpdatedAt(Instant.now().toString());

        if ("paid".equals(status)) {
            payment.setReceiptUrl("/receipts/" + payment.getId());
        }
        if (failureReason != null) {
            payment.setFailureReason(failureReason);
        }

        // If paid, also update the contract's paymentStatus
        if ("paid".equals(status)) {
            contractRepository.findById(payment.getContractId()).ifPresent(contract -> {
                contract.setPaymentStatus("paid");
                contractRepository.save(contract);
            });
        }

        return ResponseEntity.ok(paymentRepository.save(payment));
    }

    // POST /api/payments/{id}/simulate — simulate payment success/failure (80% success)
    @PostMapping("/{id}/simulate")
    public ResponseEntity<Payment> simulatePayment(@PathVariable String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        boolean success = Math.random() < 0.8;
        String now = Instant.now().toString();

        if (success) {
            payment.setStatus("paid");
            payment.setReceiptUrl("/receipts/" + payment.getId());

            // Sync payment status to the contract
            contractRepository.findById(payment.getContractId()).ifPresent(contract -> {
                contract.setPaymentStatus("paid");
                contractRepository.save(contract);
            });
        } else {
            payment.setStatus("failed");
            payment.setFailureReason("Card declined (simulated)");
        }

        payment.setUpdatedAt(now);
        return ResponseEntity.ok(paymentRepository.save(payment));
    }
}