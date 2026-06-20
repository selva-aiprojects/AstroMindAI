package com.astra.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    @GetMapping("/plans")
    public ResponseEntity<?> getSubscriptionPlans() {
        try {
            Map<String, Object> plans = Map.of(
                    "free", Map.of(
                            "name", "Free",
                            "price", 0,
                            "features", Map.of(
                                    "dailyQueries", 5,
                                    "chartAccess", "basic",
                                    "aiChat", "limited"
                            )
                    ),
                    "premium", Map.of(
                            "name", "Premium",
                            "price", 9.99,
                            "features", Map.of(
                                    "dailyQueries", "unlimited",
                                    "chartAccess", "full",
                                    "aiChat", "unlimited",
                                    "prioritySupport", true
                            )
                    ),
                    "vip", Map.of(
                            "name", "VIP",
                            "price", 29.99,
                            "features", Map.of(
                                    "dailyQueries", "unlimited",
                                    "chartAccess", "full",
                                    "aiChat", "unlimited",
                                    "prioritySupport", true,
                                    "personalConsultation", true,
                                    "exclusiveFeatures", true
                            )
                    )
            );

            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            log.error("Error fetching subscription plans", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch plans"));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionRequest request) {
        try {
            // In production, integrate with Stripe, Apple, or Google payment gateways
            log.info("Creating subscription for user: {}, plan: {}", request.getUserId(), request.getPlan());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Subscription created successfully",
                    "subscriptionId", UUID.randomUUID().toString(),
                    "plan", request.getPlan(),
                    "status", "active"
            ));
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create subscription"));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription(@RequestParam UUID userId) {
        try {
            // In production, fetch from database
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "plan", "free",
                    "status", "active",
                    "renewalDate", "2024-12-31"
            ));
        } catch (Exception e) {
            log.error("Error fetching current subscription", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch subscription"));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSubscription(@RequestParam UUID userId) {
        try {
            log.info("Cancelling subscription for user: {}", userId);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Subscription cancelled successfully",
                    "status", "cancelled"
            ));
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to cancel subscription"));
        }
    }

    @Data
    public static class SubscriptionRequest {
        private UUID userId;
        private String plan;
        private String paymentMethod;
    }
}
