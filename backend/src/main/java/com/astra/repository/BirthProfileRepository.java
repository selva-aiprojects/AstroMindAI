package com.astra.repository;

import com.astra.model.BirthProfile;
import com.astra.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BirthProfileRepository extends JpaRepository<BirthProfile, UUID> {
    List<BirthProfile> findByUser(User user);
    Optional<BirthProfile> findByUserUserId(UUID userId);
}
