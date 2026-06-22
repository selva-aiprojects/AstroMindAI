package com.astra.security;

import com.astra.model.BirthProfile;
import com.astra.model.User;
import com.astra.repository.UserRepository;
import com.astra.service.BirthProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final BirthProfileService birthProfileService;

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

    public String registerWithEmail(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with email already exists");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAuthProvider(User.AuthProvider.EMAIL);
        user.setSubscriptionTier(User.SubscriptionTier.FREE);
        user.setIsActive(true);
        user = userRepository.save(user);
        
        return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail());
    }

    public String loginWithEmail(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
                
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail());
    }

    public String authenticateDemo() {
        Optional<User> existingUser = userRepository.findByEmail("demo@astromindai.app");

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Demo login: existing user found");
        } else {
            user = new User();
            user.setEmail("demo@astromindai.app");
            user.setAuthProvider(User.AuthProvider.EMAIL);
            user.setSubscriptionTier(User.SubscriptionTier.PREMIUM);
            user.setIsActive(true);
            user = userRepository.save(user);
            log.info("Demo login: new user created");
        }

        // Auto-create demo birth profile if it doesn't exist
        try {
            if (!birthProfileService.getBirthProfileByUserId(user.getUserId()).isPresent()) {
                BirthProfile demoProfile = new BirthProfile();
                demoProfile.setUser(user);
                demoProfile.setFullName("Demo User");
                demoProfile.setGender(BirthProfile.Gender.MALE);
                demoProfile.setBirthDate(LocalDate.of(1990, 6, 15)); // Demo birth date
                demoProfile.setBirthTime(LocalTime.of(10, 30)); // Demo birth time
                demoProfile.setBirthLatitude(new java.math.BigDecimal("28.6139")); // New Delhi coordinates
                demoProfile.setBirthLongitude(new java.math.BigDecimal("77.2090"));
                demoProfile.setTimezone("Asia/Kolkata");
                demoProfile.setAyanamsa("LAHIRI");
                birthProfileService.createBirthProfile(demoProfile);
                log.info("Demo birth profile created for user: {}", user.getUserId());
            }
        } catch (Exception e) {
            log.warn("Failed to create demo birth profile: {}", e.getMessage());
            // Continue with authentication even if birth profile creation fails
        }

        return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail());
    }

    public void logout(String token) {
        // In production, add token to blacklist
        log.info("User logout requested");
    }
}
