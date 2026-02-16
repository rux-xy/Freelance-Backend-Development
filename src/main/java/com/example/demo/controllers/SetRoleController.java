package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import org.springframework.web.bind.annotation.*;
import com.example.demo.security.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/auth") //base path you want
public class SetRoleController {

    final UserRepository userRepository;
    final JwtUtil jwtUtil;

    public SetRoleController(UserRepository userRepository, JwtUtil jwtUtil) { //same thing done from @AutoWired -constructor initialization
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PatchMapping("/update-role")
    public User updateRole(@RequestHeader("Authorization") String authHeader,
                           @RequestBody Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUserId(token);
        String newRole = body.get("role");

        if (!"CLIENT".equals(newRole) && !"FREELANCER".equals(newRole)) {
            throw new RuntimeException("Invalid role");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
        return user;
    }

    //This is dangerous, because someone could send a different email and change another user’s role.
    // Security issue: trusting the email in /update-role
    /*@PatchMapping("/update-role")
    public User updateRole(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String newRole = body.get("role"); // "CLIENT" or "FREELANCER"

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        userRepository.save(user);
        return user;
    }*/
}
