package com.example.demo.controllers;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.GoogleAuthRequest;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RegisterRequest;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${spring.mongodb.uri}")
    private String mongoUri;

    @PostMapping("/testMongo")
    public String testMongo() {
        System.out.println("Mongo URI being used: " + mongoUri);
        return "Check console!";
    }


    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request){
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userRepository.save(user);
        return "User registered successfully";
    }


    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    @Value("${google.client-id}")
    private String googleClientId;

    @PostMapping("/google")
    public AuthResponse googleAuth(@RequestBody GoogleAuthRequest request) throws Exception {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(request.getToken());

        if (idToken == null) {
            throw new RuntimeException("Invalid Google token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(""); // no password for Google users
            user.setRole("USER");
            userRepository.save(user);
        }

        String jwt = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(jwt);
    }


}
