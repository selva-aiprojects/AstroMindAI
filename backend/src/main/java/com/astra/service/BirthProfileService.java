package com.astra.service;

import com.astra.model.BirthProfile;
import com.astra.model.User;
import com.astra.repository.BirthProfileRepository;
import com.astra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BirthProfileService {

    private final BirthProfileRepository birthProfileRepository;
    private final UserRepository userRepository;

    public BirthProfile createBirthProfile(BirthProfile birthProfile) {
        if (birthProfile.getUser() != null && birthProfile.getUser().getUserId() != null) {
            UUID userId = birthProfile.getUser().getUserId();
            // Auto-create user if it doesn't exist to prevent foreign key errors after server restarts
            if (!userRepository.existsById(userId)) {
                User defaultUser = new User();
                defaultUser.setUserId(userId);
                defaultUser.setEmail("demo@astromindai.app");
                defaultUser.setAuthProvider(User.AuthProvider.EMAIL);
                defaultUser.setSubscriptionTier(User.SubscriptionTier.PREMIUM);
                defaultUser.setIsActive(true);
                defaultUser.setCreatedAt(java.time.LocalDateTime.now());
                defaultUser.setUpdatedAt(java.time.LocalDateTime.now());
                userRepository.save(defaultUser);
            }

            Optional<BirthProfile> existingOpt = birthProfileRepository.findByUserUserId(userId);
            if (existingOpt.isPresent()) {
                BirthProfile existing = existingOpt.get();
                existing.setFullName(birthProfile.getFullName());
                existing.setGender(birthProfile.getGender());
                existing.setBirthDate(birthProfile.getBirthDate());
                existing.setBirthTime(birthProfile.getBirthTime());
                existing.setBirthLatitude(birthProfile.getBirthLatitude());
                existing.setBirthLongitude(birthProfile.getBirthLongitude());
                existing.setTimezone(birthProfile.getTimezone());
                if (birthProfile.getAyanamsa() != null) {
                    existing.setAyanamsa(birthProfile.getAyanamsa());
                }
                return birthProfileRepository.save(existing);
            }
        }
        return birthProfileRepository.save(birthProfile);
    }

    public Optional<BirthProfile> getBirthProfileById(UUID profileId) {
        return birthProfileRepository.findById(profileId);
    }

    public List<BirthProfile> getBirthProfilesByUser(User user) {
        return birthProfileRepository.findByUser(user);
    }

    public Optional<BirthProfile> getBirthProfileByUserId(UUID userId) {
        return birthProfileRepository.findByUserUserId(userId);
    }

    public BirthProfile updateBirthProfile(BirthProfile birthProfile) {
        return birthProfileRepository.save(birthProfile);
    }

    public void deleteBirthProfile(UUID profileId) {
        birthProfileRepository.deleteById(profileId);
    }
}
