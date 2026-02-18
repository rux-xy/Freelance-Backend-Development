package com.example.demo.controllers;

import com.example.demo.entities.Contract;
import com.example.demo.entities.User;
import com.example.demo.repositories.ContractRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/client/{clientId}")
    public List<Contract> getClientContracts(@PathVariable String clientId){
        return contractRepository.findByClientId(clientId);
    }
    @GetMapping("/freelancer/{freelancerId}")
    public List<Contract> getFreelancerContracts(@PathVariable String freelancerId){
            return contractRepository.findByFreelancerId(freelancerId);
    }

    @GetMapping("/my")
    public List<Contract> getMyContracts(@RequestHeader("Authorization") String authHeader) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        if ("CLIENT".equals(user.getRole())) {
            return contractRepository.findByClientId(user.getId());
        } else {
            return contractRepository.findByFreelancerId(user.getId());
        }
    }

    @GetMapping("/{id}")
    public Contract getContract(@PathVariable String id){
        return contractRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Contract not found"));
    }

    @PatchMapping("/{id}/status")
    public Contract updateStatus(@PathVariable String id, @RequestParam String status) {
        Contract contract = contractRepository.findById(id).orElseThrow();
        contract.setStatus(status);
        if ("completed".equals(status)) contract.setCompletedAt(Instant.now().toString());
        return contractRepository.save(contract);
    }

    @PatchMapping("/{id}/payment-status")
    public Contract updatePaymentStatus(@PathVariable String id, @RequestParam String paymentStatus) {
        Contract contract = contractRepository.findById(id).orElseThrow();
        contract.setPaymentStatus(paymentStatus);
        return contractRepository.save(contract);
    }

    @GetMapping("/all")
    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }
}
