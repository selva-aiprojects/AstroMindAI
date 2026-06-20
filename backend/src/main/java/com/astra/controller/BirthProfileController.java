package com.astra.controller;

import com.astra.model.BirthProfile;
import com.astra.service.BirthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/birth-profile")
@RequiredArgsConstructor
public class BirthProfileController {

    private final BirthProfileService birthProfileService;

    @PostMapping
    public ResponseEntity<BirthProfile> createBirthProfile(@RequestBody BirthProfile birthProfile) {
        BirthProfile created = birthProfileService.createBirthProfile(birthProfile);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<BirthProfile> getBirthProfile(@RequestParam UUID userId) {
        return birthProfileService.getBirthProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<BirthProfile> updateBirthProfile(@RequestBody BirthProfile birthProfile) {
        BirthProfile updated = birthProfileService.updateBirthProfile(birthProfile);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBirthProfile(@RequestParam UUID profileId) {
        birthProfileService.deleteBirthProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}
