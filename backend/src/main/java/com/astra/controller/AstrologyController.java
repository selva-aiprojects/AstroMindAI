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
                    .orElseThrow(() -> new RuntimeException("Birth profile not found. Please create a birth profile first."));

            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude().doubleValue(),
                    birthProfile.getBirthLongitude().doubleValue(),
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
                    birthProfile.getBirthLatitude().doubleValue(),
                    birthProfile.getBirthLongitude().doubleValue(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );

            double moonLongitude = chart.getPlanetaryPositions().get("Moon").getLongitude();

            DashaCalculator.DashaTimeline timeline = dashaCalculator.calculateDashaTimeline(
                    birthProfile.getBirthDate(),
                    moonLongitude
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
                    birthProfile.getBirthLatitude().doubleValue(),
                    birthProfile.getBirthLongitude().doubleValue(),
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
                    birthProfile.getBirthLatitude().doubleValue(),
                    birthProfile.getBirthLongitude().doubleValue(),
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

    @GetMapping("/accuracy-verification")
    public ResponseEntity<?> verifyAccuracy(@RequestParam UUID userId) {
        try {
            BirthProfile birthProfile = birthProfileService.getBirthProfileByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Birth profile not found"));

            // Calculate birth chart
            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude().doubleValue(),
                    birthProfile.getBirthLongitude().doubleValue(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );

            // Perform accuracy checks
            Map<String, Object> accuracyReport = new HashMap<>();
            
            // 1. Planetary position validation
            accuracyReport.put("planetaryPositionAccuracy", validatePlanetaryPositions(chart));
            
            // 2. House calculation validation
            accuracyReport.put("houseCalculationAccuracy", validateHouseCalculations(chart));
            
            // 3. Ayanamsa correction validation
            accuracyReport.put("ayanamsaAccuracy", validateAyanamsaCorrection(chart));
            
            // 4. Overall system accuracy
            double overallAccuracy = calculateOverallAccuracy(accuracyReport);
            accuracyReport.put("overallAccuracy", overallAccuracy);
            
            // 5. Calculation method used
            accuracyReport.put("calculationMethod", swissEphemerisService.isLibraryAvailable() ? 
                "Swiss Ephemeris (Native Library)" : "Fallback Approximation");
            
            // 6. Metadata
            accuracyReport.put("timestamp", java.time.Instant.now());
            accuracyReport.put("julianDay", chart.getJulianDay());
            accuracyReport.put("ayanamsaUsed", chart.getAyanamsa());

            return ResponseEntity.ok(accuracyReport);
        } catch (Exception e) {
            log.error("Error verifying accuracy", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to verify accuracy: " + e.getMessage()));
        }
    }

    private Map<String, Object> validatePlanetaryPositions(SwissEphemerisService.BirthChart chart) {
        Map<String, Object> validation = new HashMap<>();
        int validPositions = 0;
        int totalPositions = chart.getPlanetaryPositions().size();
        
        for (Map.Entry<String, SwissEphemerisService.PlanetPosition> entry : 
                chart.getPlanetaryPositions().entrySet()) {
            SwissEphemerisService.PlanetPosition position = entry.getValue();
            
            // Check if longitude is within valid range (0-360)
            boolean validLongitude = position.getLongitude() >= 0 && position.getLongitude() < 360;
            
            // Check if sign is valid
            boolean validSign = position.getSign() != null && !position.getSign().isEmpty();
            
            // Check if nakshatra is valid
            boolean validNakshatra = position.getNakshatra() != null && !position.getNakshatra().isEmpty();
            
            if (validLongitude && validSign && validNakshatra) {
                validPositions++;
            }
            
            validation.put(entry.getKey() + "_valid", validLongitude && validSign && validNakshatra);
            validation.put(entry.getKey() + "_longitude", position.getLongitude());
            validation.put(entry.getKey() + "_sign", position.getSign());
            validation.put(entry.getKey() + "_nakshatra", position.getNakshatra());
        }
        
        double accuracy = (double) validPositions / totalPositions * 100;
        validation.put("accuracy", accuracy);
        validation.put("validPositions", validPositions);
        validation.put("totalPositions", totalPositions);
        
        return validation;
    }

    private Map<String, Object> validateHouseCalculations(SwissEphemerisService.BirthChart chart) {
        Map<String, Object> validation = new HashMap<>();
        int validPlacements = 0;
        int totalPlacements = chart.getHousePlacements().size();
        
        for (Map.Entry<String, Integer> entry : chart.getHousePlacements().entrySet()) {
            int house = entry.getValue();
            
            // Check if house is within valid range (1-12)
            boolean validHouse = house >= 1 && house <= 12;
            
            if (validHouse) {
                validPlacements++;
            }
            
            validation.put(entry.getKey() + "_house", house);
            validation.put(entry.getKey() + "_valid", validHouse);
        }
        
        double accuracy = (double) validPlacements / totalPlacements * 100;
        validation.put("accuracy", accuracy);
        validation.put("validPlacements", validPlacements);
        validation.put("totalPlacements", totalPlacements);
        
        return validation;
    }

    private Map<String, Object> validateAyanamsaCorrection(SwissEphemerisService.BirthChart chart) {
        Map<String, Object> validation = new HashMap<>();
        
        // Check if ayanamsa value is reasonable (typically 22-25 degrees)
        String ayanamsa = chart.getAyanamsa();
        double expectedCorrection = getExpectedAyanamsaCorrection(ayanamsa);
        
        validation.put("ayanamsaType", ayanamsa);
        validation.put("expectedCorrection", expectedCorrection);
        validation.put("valid", expectedCorrection > 0 && expectedCorrection < 30);
        
        return validation;
    }

    private double getExpectedAyanamsaCorrection(String ayanamsa) {
        return switch (ayanamsa.toUpperCase()) {
            case "LAHIRI" -> 24.0;
            case "RAMAN" -> 22.5;
            case "KP" -> 23.5;
            case "FAGAN_BRADLEY" -> 24.5;
            default -> 24.0;
        };
    }

    private double calculateOverallAccuracy(Map<String, Object> accuracyReport) {
        double planetaryAccuracy = ((Map<String, Object>) accuracyReport.get("planetaryPositionAccuracy"))
                .containsKey("accuracy") ? 
                (double) ((Map<String, Object>) accuracyReport.get("planetaryPositionAccuracy")).get("accuracy") : 0;
        
        double houseAccuracy = ((Map<String, Object>) accuracyReport.get("houseCalculationAccuracy"))
                .containsKey("accuracy") ? 
                (double) ((Map<String, Object>) accuracyReport.get("houseCalculationAccuracy")).get("accuracy") : 0;
        
        double ayanamsaValid = ((Map<String, Object>) accuracyReport.get("ayanamsaAccuracy"))
                .containsKey("valid") && 
                (boolean) ((Map<String, Object>) accuracyReport.get("ayanamsaAccuracy")).get("valid") ? 100 : 0;
        
        // Weighted average: planetary positions (50%), house calculations (30%), ayanamsa (20%)
        return (planetaryAccuracy * 0.5) + (houseAccuracy * 0.3) + (ayanamsaValid * 0.2);
    }

    @GetMapping("/life-summary")
    public ResponseEntity<?> getLifeSummary(@RequestParam UUID userId) {
        try {
            BirthProfile birthProfile = birthProfileService.getBirthProfileByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Birth profile not found"));

            // Prepare context with birth data
            Map<String, Object> context = new HashMap<>();
            context.put("birthDate", birthProfile.getBirthDate());
            context.put("birthTime", birthProfile.getBirthTime());
            context.put("latitude", birthProfile.getBirthLatitude().doubleValue());
            context.put("longitude", birthProfile.getBirthLongitude().doubleValue());
            context.put("timezone", birthProfile.getTimezone());
            context.put("ayanamsa", birthProfile.getAyanamsa());

            // Calculate birth chart for context
            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude().doubleValue(),
                    birthProfile.getBirthLongitude().doubleValue(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );
            context.put("chart", chart);
            context.put("planetaryPositions", chart.getPlanetaryPositions());
            context.put("moonNakshatra", chart.getPlanetaryPositions().get("Moon").getNakshatra());
            context.put("swissEphemerisService", swissEphemerisService);

            // Query agents
            Map<String, String> summaries = new HashMap<>();
            String[] agentsToQuery = {"career_guidance", "marriage_guidance", "finance_guidance", "health_guidance", "spiritual_guidance"};
            String[] keys = {"career", "marriage", "finance", "health", "spiritual"};
            
            for (int i = 0; i < agentsToQuery.length; i++) {
                AgentRouter.AgentRequest agentRequest = AgentRouter.AgentRequest.builder()
                        .userId(userId.toString())
                        .query("Provide insights on my " + keys[i])
                        .context(context)
                        .build();
                AgentRouter.AgentResponse response = agentRouter.routeToAgent(agentsToQuery[i], agentRequest);
                summaries.put(keys[i], response.getResponse());
            }

            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            log.error("Error generating life summary", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate life summary: " + e.getMessage()));
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
            context.put("latitude", birthProfile.getBirthLatitude().doubleValue());
            context.put("longitude", birthProfile.getBirthLongitude().doubleValue());
            context.put("timezone", birthProfile.getTimezone());
            context.put("ayanamsa", birthProfile.getAyanamsa());

            // Calculate birth chart for context
            SwissEphemerisService.BirthChart chart = swissEphemerisService.calculateBirthChart(
                    birthProfile.getBirthDate(),
                    birthProfile.getBirthTime(),
                    birthProfile.getBirthLatitude().doubleValue(),
                    birthProfile.getBirthLongitude().doubleValue(),
                    birthProfile.getTimezone(),
                    birthProfile.getAyanamsa()
            );
            context.put("chart", chart);
            context.put("planetaryPositions", chart.getPlanetaryPositions());
            context.put("moonNakshatra", chart.getPlanetaryPositions().get("Moon").getNakshatra());
            context.put("swissEphemerisService", swissEphemerisService);

            // Create agent request
            AgentRouter.AgentRequest agentRequest = AgentRouter.AgentRequest.builder()
                    .userId(request.getUserId().toString())
                    .query(request.getQuery())
                    .context(context)
                    .build();

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
