package com.example.demo.dtos;

import com.example.demo.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private User user;
    private String message;
}
