package com.ensah.nlp_annotation_platform.controller;

import com.ensah.nlp_annotation_platform.config.JwtUtils;
import com.ensah.nlp_annotation_platform.domain.RefreshToken;
import com.ensah.nlp_annotation_platform.domain.User;
import com.ensah.nlp_annotation_platform.dto.request.LoginRequest;
import com.ensah.nlp_annotation_platform.dto.request.RefreshTokenRequest;
import com.ensah.nlp_annotation_platform.dto.response.AuthResponse;
import com.ensah.nlp_annotation_platform.repository.RefreshTokenRepository;
import com.ensah.nlp_annotation_platform.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          RefreshTokenRepository refreshTokenRepository,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("");

            User user = userRepository.findByUsername(username).orElseThrow();
            String token = jwtUtils.generateToken(username, role);
            String refreshToken = createRefreshToken(user);

            return ResponseEntity.ok(new AuthResponse(token, refreshToken, username, role));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var rtOpt = refreshTokenRepository.findByToken(request.getRefreshToken());

        if (rtOpt.isEmpty() || rtOpt.get().getExpiry().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_refresh_token"));
        }

        RefreshToken oldRt = rtOpt.get();
        User user = oldRt.getUser();

        refreshTokenRepository.delete(oldRt);

        String role = user.getRoles() == null ? "" :
                java.util.Arrays.stream(user.getRoles().split(","))
                        .map(String::trim)
                        .filter(r -> !r.isEmpty())
                        .findFirst()
                        .orElse("");
        String newToken = jwtUtils.generateToken(user.getUsername(), role);
        String newRefreshToken = createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(newToken, newRefreshToken, user.getUsername(), role));
    }

    private String createRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiry(Instant.now().plusSeconds(604800));
        refreshTokenRepository.save(rt);
        return rt.getToken();
    }
}
