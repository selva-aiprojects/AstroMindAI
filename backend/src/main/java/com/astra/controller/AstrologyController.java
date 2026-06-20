package com.astra.controller;

import com.astra.ai.AgentRouter;
import com.astra.astrology.DashaCalculator;
import com.astra.astrology.SwissEphemerisService;
import com.astra.astrology.TransitCalculator;
import com.astra.model.BirthProfile;
import com.astra.service.BirthProfileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/astrology")
@RequiredArgsConstructor
@Slf4j
public class AstrologyController {

    private final SwissEphemerisService swissEphemerisService;
    private final DashaCalculator dashaCalculator;
    private final TransitCalculator transitCalculator;
    private final BirthProfileService birthProfileService;
    private final AgentRouter agentRouter;

    @GetMapping("/birth-chart")
    public ResponseEntity<?> getBirthChart(@RequestParam UUID userId) {
        try {
            BirthProfile birthProfile = birthProfileService.getBirthProfileByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Birth profile not found"));

            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude(),
                    birthProfile.getBirthLongitude(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );

            return ResponseEntity.ok(chart);
        } catch (Exception e) {
            log.error("Error generating birth chart", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate birth chart"));
        }
    }

    @GetMapping("/dasha-timeline")
    public ResponseEntity<?> getDashaTimeline(@RequestParam UUID userId) {
        try {
            BirthProfile birthProfile = birthProfileService.getBirthProfileByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Birth profile not found"));

            // Calculate moon nakshatra from birth chart
            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude(),
                    birthProfile.getBirthLongitude(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );

            String moonNakshatra = chart.getPlanetaryPositions().get("Moon").getNakshatra();

            DashaCalculator.DashaTimeline timeline = dashaCalculator.calculateDashaTimeline(
                    birthProfile.getBirthDate(),
                    moonNakshatra
            );

            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Error generating Dasha timeline", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate Dasha timeline"));
        }
    }

    @GetMapping("/transits")
    public ResponseEntity<?> getTransits(@RequestParam UUID userId) {
        try {
            BirthProfile birthProfile = birthProfileService.getBirthProfileByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Birth profile not found"));

            // Calculate natal chart
            SwissEphemerisService.BirthChart natalChart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude(),
                    birthProfile.getBirthLongitude(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );

            // Calculate transits
            TransitCalculator.TransitAnalysis analysis = transitCalculator.calculateTransits(
                    LocalDate.now(),
                    natalChart.getPlanetaryPositions()
            );

            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Error calculating transits", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to calculate transits"));
        }
    }

    @GetMapping("/daily-horoscope")
    public ResponseEntity<?> getDailyHoroscope(@RequestParam UUID userId) {
        try {
            BirthProfile birthProfile = birthProfileService.getBirthProfileByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Birth profile not found"));

            // Calculate natal chart
            SwissEphemerisService.BirthChart natalChart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude(),
                    birthProfile.getBirthLongitude(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );

            // Calculate transits
            TransitCalculator.TransitAnalysis analysis = transitCalculator.calculateTransits(
                    LocalDate.now(),
                    natalChart.getPlanetaryPositions()
            );

            // Generate daily horoscope based on transits
            String horoscope = generateDailyHoroscope(natalChart, analysis);

            return ResponseEntity.ok(Map.of(
                    "date", LocalDate.now(),
                    "horoscope", horoscope,
                    "moonSign", natalChart.getPlanetaryPositions().get("Moon").getSign(),
                    "ascendant", calculateSign(natalChart.getAscendant())
            ));
        } catch (Exception e) {
            log.error("Error generating daily horoscope", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate daily horoscope"));
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        try {
            BirthProfile birthProfile = birthProfileService.getBirthProfileByUserId(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Birth profile not found"));

            // Prepare context with birth data
            Map<String, Object> context = new HashMap<>();
            context.put("birthDate", birthProfile.getBirthDate());
            context.put("birthTime", birthProfile.getBirthTime());
            context.put("latitude", birthProfile.getBirthLatitude());
            context.put("longitude", birthProfile.getBirthLongitude());
            context.put("timezone", birthProfile.getTimezone());
            context.put("ayanamsa", birthProfile.getAyanamsa());

            // Calculate birth chart for context
            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude(),
                    birthProfile.getBirthLongitude(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );
            context.put("chart", chart);
            context.put("planetaryPositions", chart.getPlanetaryPositions());
            context.put("moonNakshatra", chart.getPlanetaryPositions().get("Moon").getNakshatra());

            // Create agent request
            AgentRouter.AgentRequest agentRequest = new AgentRouter.AgentRequest();
            agentRequest.setUserId(request.getUserId().toString());
            agentRequest.setQuery(request.getQuery());
            agentRequest.setContext(context);

            // Route to appropriate agent
            AgentRouter.AgentResponse response = agentRouter.routeQuery(agentRequest);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process chat request"));
        }
    }

    private String generateDailyHoroscope(SwissEphemerisService.BirthChart natalChart, 
                                          TransitCalculator.TransitAnalysis analysis) {
        StringBuilder horoscope = new StringBuilder();
        
        horoscope.append("🌟 **Daily Horoscope for ").append(LocalDate.now()).append("**\n\n");
        horoscope.append("**Moon Sign:** ").append(natalChart.getPlanetaryPositions().get("Moon").getSign()).append("\n");
        horoscope.append("**Ascendant:** ").append(calculateSign(natalChart.getAscendant())).append("\n\n");
        
        horoscope.append("**Today's Themes:**\n");
        horoscope.append("- Focus on your career and professional growth today.\n");
        horoscope.append("- Relationships may require attention and communication.\n");
        horoscope.append("- Financial matters look favorable for decision-making.\n");
        horoscope.append("- Health and wellness should be a priority.\n\n");
        
        horoscope.append("**Lucky Color:** Blue\n");
        horoscope.append("**Lucky Number:** 7\n");
        horoscope.append("**Lucky Time:** 10:00 AM - 12:00 PM\n\n");
        
        if (!analysis.getMajorTransits().isEmpty()) {
            horoscope.append("**Active Major Transits:**\n");
            for (Map.Entry<String, String> entry : analysis.getMajorTransits().entrySet()) {
                horoscope.append("- ").append(entry.getValue()).append("\n");
            }
        }
        
        return horoscope.toString();
    }

    private String calculateSign(double longitude) {
        int signIndex = (int)(longitude / 30);
        String[] signs = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        };
        return signs[signIndex];
    }

    @Data
    public static class ChatRequest {
        private UUID userId;
        private String query;
    }
}
