package com.example.demo.controllers;

import com.example.demo.dtos.*;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getBio());
    }

    @Value("${spring.mongodb.uri}")
    private String mongoUri;

    @PostMapping("/testMongo")
    public String testMongo() {
        System.out.println("Mongo URI being used: " + mongoUri);
        return "Check console!";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, null, "Email already exists"));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("PENDING");
        user.setCreatedAt(Instant.now().toString());
        user.setUpdatedAt(Instant.now().toString());

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, toResponse(user), "User registered successfully"));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Invalid email or password"));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(
                new AuthResponse(jwtToken, toResponse(user), "Login successful")
        );
    }

    @Value("${google.client-id}")
    private String googleClientId;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody GoogleAuthRequest request) throws Exception {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(request.getToken());

        if (idToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, "Invalid Google token"));
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPassword(""); // Google users don't use password
            user.setRole("PENDING");
            user.setCreatedAt(Instant.now().toString());
            user.setUpdatedAt(Instant.now().toString());
            userRepository.save(user);
        }

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(
                new AuthResponse(jwtToken, toResponse(user), "Google login successful")
        );
    }


    @GetMapping("/me") //Gets user object
    public UserResponse getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUserId(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toResponse(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        String email = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow();
        if (body.containsKey("name")) user.setName(body.get("name"));
        if (body.containsKey("bio")) user.setBio(body.get("bio"));
        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    @GetMapping("/keepalive")
    public String keepAlive(){
        return "Breathing";
    }


}
