package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.dto.UserDto;
import com.ensah.nlp_annotation_platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        UserDto user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }
        // Placeholder: real implementation should validate password and return JWT
        return ResponseEntity.ok(Map.of("username", user.getUsername()));
    }
}
