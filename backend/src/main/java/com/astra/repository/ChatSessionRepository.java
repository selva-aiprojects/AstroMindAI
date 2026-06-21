package com.astra.repository;

import com.astra.model.ChatSession;
import com.astra.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUser(User user);
    List<ChatSession> findByUserUserId(UUID userId);
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);
}
