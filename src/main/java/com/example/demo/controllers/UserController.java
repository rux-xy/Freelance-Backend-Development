package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/users — list all users (admin only)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // PATCH /api/users/{id}/role — update role
    @PatchMapping("/{id}/role")
    public ResponseEntity<Map<String, String>> updateRole(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(body.get("role").toUpperCase());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("id", user.getId(), "role", user.getRole()));
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/seed-admin")
    public ResponseEntity<String> seedAdmin() {
        if (userRepository.findByEmail("admin@freelance.com").isPresent()) {
            return ResponseEntity.ok("Admin already exists");
        }
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@freelancehub.com");
        admin.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin@123"));
        admin.setRole("ADMIN");
        userRepository.save(admin);
        return ResponseEntity.ok("Admin created");
    }
}