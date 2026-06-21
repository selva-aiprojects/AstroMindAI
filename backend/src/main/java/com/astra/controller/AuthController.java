package com.astra.controller;

import com.astra.security.AuthenticationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody GoogleAuthRequest request) {
        try {
            String token = authenticationService.authenticateWithGoogle(
                    request.getGoogleId(),
                    request.getEmail(),
                    request.getName()
            );
            return ResponseEntity.ok(Map.of("token", token, "provider", "google"));
        } catch (Exception e) {
            log.error("Google authentication failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed"));
        }
    }

    @PostMapping("/phone/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody PhoneOtpRequest request) {
        try {
            // In production, integrate with Firebase to send OTP
            log.info("OTP send request for phone: {}", request.getPhone());
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            log.error("Failed to send OTP", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to send OTP"));
        }
    }

    @PostMapping("/phone/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody PhoneVerifyRequest request) {
        try {
            String token = authenticationService.authenticateWithPhone(request.getPhone(), request.getOtp());
            return ResponseEntity.ok(Map.of("token", token, "provider", "phone"));
        } catch (Exception e) {
            log.error("Phone authentication failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication failed"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String newToken = authenticationService.refreshToken(request.getToken());
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        try {
            authenticationService.logout(request.getToken());
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Logout failed"));
        }
    }

    @PostMapping("/demo")
    public ResponseEntity<?> demoLogin() {
        try {
            String token = authenticationService.authenticateDemo();
            return ResponseEntity.ok(Map.of("token", token, "provider", "demo"));
        } catch (Exception e) {
            log.error("Demo login failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Demo login failed"));
        }
    }

    @Data
    public static class GoogleAuthRequest {
        private String googleId;
        private String email;
        private String name;
    }

    @Data
    public static class PhoneOtpRequest {
        private String phone;
    }

    @Data
    public static class PhoneVerifyRequest {
        private String phone;
        private String otp;
    }

    @Data
    public static class RefreshTokenRequest {
        private String token;
    }

    @Data
    public static class LogoutRequest {
        private String token;
    }
}
