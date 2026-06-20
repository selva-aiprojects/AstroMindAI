package com.astra.service;

import com.astra.model.BirthProfile;
import com.astra.model.User;
import com.astra.repository.BirthProfileRepository;
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

    public BirthProfile createBirthProfile(BirthProfile birthProfile) {
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
