package com.astra.security;

import com.astra.model.User;
import com.astra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public String authenticateWithGoogle(String googleId, String email, String name) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Existing user found: {}", email);
        } else {
            user = new User();
            user.setEmail(email);
            user.setAuthProvider(User.AuthProvider.GOOGLE);
            user.setSubscriptionTier(User.SubscriptionTier.FREE);
            user.setIsActive(true);
            user = userRepository.save(user);
            log.info("New user created: {}", email);
        }
        
        return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail());
    }

    public String authenticateWithPhone(String phone, String otp) {
        // In production, verify OTP with Firebase
        // For now, we'll implement a simplified version
        
        Optional<User> existingUser = userRepository.findByPhone(phone);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Existing user found with phone: {}", phone);
        } else {
            user = new User();
            user.setPhone(phone);
            user.setAuthProvider(User.AuthProvider.PHONE);
            user.setSubscriptionTier(User.SubscriptionTier.FREE);
            user.setIsActive(true);
            user = userRepository.save(user);
            log.info("New user created with phone: {}", phone);
        }
        
        return jwtTokenProvider.generateToken(user.getUserId(), user.getPhone());
    }

    public String refreshToken(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isPresent()) {
                return jwtTokenProvider.generateToken(user.get().getUserId(), user.get().getEmail());
            }
        }
        
        throw new RuntimeException("Invalid token");
    }

    public void logout(String token) {
        // In production, add token to blacklist
        log.info("User logout requested");
    }
}
