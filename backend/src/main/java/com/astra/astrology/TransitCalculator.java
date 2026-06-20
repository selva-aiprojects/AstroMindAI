package com.astra.astrology;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TransitCalculator {

    public TransitAnalysis calculateTransits(LocalDate currentDate, Map<String, SwissEphemerisService.PlanetPosition> natalPositions) {
        log.info("Calculating transits for date: {}", currentDate);

        // Calculate current planetary positions
        Map<String, SwissEphemerisService.PlanetPosition> transitPositions = calculateCurrentPositions(currentDate);
        
        // Calculate aspects between transit and natal positions
        Map<String, TransitAspect> aspects = calculateAspects(transitPositions, natalPositions);
        
        // Check for major transits
        Map<String, String> majorTransits = checkMajorTransits(transitPositions, natalPositions);
        
        return TransitAnalysis.builder()
                .transitDate(currentDate)
                .transitPositions(transitPositions)
                .aspects(aspects)
                .majorTransits(majorTransits)
                .build();
    }

    private Map<String, SwissEphemerisService.PlanetPosition> calculateCurrentPositions(LocalDate currentDate) {
        Map<String, SwissEphemerisService.PlanetPosition> positions = new HashMap<>();
        
        // Placeholder calculation - in production, use Swiss Ephemeris
        double julianDay = convertToJulianDay(currentDate);
        
        positions.put("Sun", calculateTransitPosition(0, julianDay));
        positions.put("Moon", calculateTransitPosition(1, julianDay));
        positions.put("Mars", calculateTransitPosition(2, julianDay));
        positions.put("Mercury", calculateTransitPosition(3, julianDay));
        positions.put("Jupiter", calculateTransitPosition(4, julianDay));
        positions.put("Venus", calculateTransitPosition(5, julianDay));
        positions.put("Saturn", calculateTransitPosition(6, julianDay));
        positions.put("Rahu", calculateTransitPosition(10, julianDay));
        positions.put("Ketu", calculateTransitPosition(11, julianDay));
        
        return positions;
    }

    private SwissEphemerisService.PlanetPosition calculateTransitPosition(int planet, double julianDay) {
        // Placeholder calculation
        double longitude = (julianDay * (planet + 1) * 0.9856) % 360;
        if (longitude < 0) longitude += 360;
        
        return SwissEphemerisService.PlanetPosition.builder()
                .longitude(longitude)
                .nakshatra(calculateNakshatra(longitude))
                .sign(calculateSign(longitude))
                .isRetrograde(false)
                .isCombust(false)
                .build();
    }

    private Map<String, TransitAspect> calculateAspects(
            Map<String, SwissEphemerisService.PlanetPosition> transitPositions,
            Map<String, SwissEphemerisService.PlanetPosition> natalPositions) {
        
        Map<String, TransitAspect> aspects = new HashMap<>();
        
        for (Map.Entry<String, SwissEphemerisService.PlanetPosition> transitEntry : transitPositions.entrySet()) {
            for (Map.Entry<String, SwissEphemerisService.PlanetPosition> natalEntry : natalPositions.entrySet()) {
                String key = transitEntry.getKey() + " over " + natalEntry.getKey();
                double difference = Math.abs(transitEntry.getValue().getLongitude() - natalEntry.getValue().getLongitude());
                
                if (difference > 180) {
                    difference = 360 - difference;
                }
                
                String aspectType = determineAspectType(difference);
                if (aspectType != null) {
                    aspects.put(key, TransitAspect.builder()
                            .transitPlanet(transitEntry.getKey())
                            .natalPlanet(natalEntry.getKey())
                            .aspectType(aspectType)
                            .orb(difference)
                            .build());
                }
            }
        }
        
        return aspects;
    }

    private String determineAspectType(double difference) {
        // Vedic aspects (Drishti)
        if (difference < 5) return "Conjunction";
        if (Math.abs(difference - 60) < 5) return "Sextile";
        if (Math.abs(difference - 90) < 5) return "Square";
        if (Math.abs(difference - 120) < 5) return "Trine";
        if (Math.abs(difference - 180) < 5) return "Opposition";
        
        return null;
    }

    private Map<String, String> checkMajorTransits(
            Map<String, SwissEphemerisService.PlanetPosition> transitPositions,
            Map<String, SwissEphemerisService.PlanetPosition> natalPositions) {
        
        Map<String, String> majorTransits = new HashMap<>();
        
        // Check Saturn Sade Sati
        SwissEphemerisService.PlanetPosition natalMoon = natalPositions.get("Moon");
        SwissEphemerisService.PlanetPosition transitSaturn = transitPositions.get("Saturn");
        
        if (natalMoon != null && transitSaturn != null) {
            double moonLongitude = natalMoon.getLongitude();
            double saturnLongitude = transitSaturn.getLongitude();
            double difference = Math.abs(moonLongitude - saturnLongitude);
            if (difference > 180) difference = 360 - difference;
            
            if (difference < 15) {
                majorTransits.put("Saturn Sade Sati", "Saturn is within 15 degrees of natal Moon - Sade Sati period");
            }
        }
        
        // Check Jupiter transit
        SwissEphemerisService.PlanetPosition transitJupiter = transitPositions.get("Jupiter");
        if (natalMoon != null && transitJupiter != null) {
            double moonLongitude = natalMoon.getLongitude();
            double jupiterLongitude = transitJupiter.getLongitude();
            double difference = Math.abs(moonLongitude - jupiterLongitude);
            if (difference > 180) difference = 360 - difference;
            
            if (difference < 5) {
                majorTransits.put("Jupiter Transit", "Jupiter is conjunct natal Moon - favorable period");
            }
        }
        
        // Check Rahu/Ketu transits
        SwissEphemerisService.PlanetPosition transitRahu = transitPositions.get("Rahu");
        if (natalMoon != null && transitRahu != null) {
            double moonLongitude = natalMoon.getLongitude();
            double rahuLongitude = transitRahu.getLongitude();
            double difference = Math.abs(moonLongitude - rahuLongitude);
            if (difference > 180) difference = 360 - difference;
            
            if (difference < 5) {
                majorTransits.put("Rahu Transit", "Rahu is conjunct natal Moon - transformative period");
            }
        }
        
        return majorTransits;
    }

    private double convertToJulianDay(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        
        int a = year / 100;
        int b = 2 - a + a / 4;
        
        return (int)(365.25 * (year + 4716)) + (int)(30.6001 * (month + 1)) + day + b - 1524.5;
    }

    private String calculateNakshatra(double longitude) {
        double nakshatraSize = 360.0 / 27.0;
        int nakshatraIndex = (int)(longitude / nakshatraSize);
        
        String[] nakshatras = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira",
            "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
            "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
            "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
            "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
            "Uttara Bhadrapada", "Revati"
        };
        
        return nakshatras[nakshatraIndex];
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
    public static class TransitAnalysis {
        private LocalDate transitDate;
        private Map<String, SwissEphemerisService.PlanetPosition> transitPositions;
        private Map<String, TransitAspect> aspects;
        private Map<String, String> majorTransits;
        
        public static TransitAnalysisBuilder builder() {
            return new TransitAnalysisBuilder();
        }
    }

    @Data
    public static class TransitAspect {
        private String transitPlanet;
        private String natalPlanet;
        private String aspectType;
        private double orb;
        
        public static TransitAspectBuilder builder() {
            return new TransitAspectBuilder();
        }
    }

    public static class TransitAnalysisBuilder {
        private LocalDate transitDate;
        private Map<String, SwissEphemerisService.PlanetPosition> transitPositions;
        private Map<String, TransitAspect> aspects;
        private Map<String, String> majorTransits;

        public TransitAnalysisBuilder transitDate(LocalDate transitDate) {
            this.transitDate = transitDate;
            return this;
        }

        public TransitAnalysisBuilder transitPositions(Map<String, SwissEphemerisService.PlanetPosition> transitPositions) {
            this.transitPositions = transitPositions;
            return this;
        }

        public TransitAnalysisBuilder aspects(Map<String, TransitAspect> aspects) {
            this.aspects = aspects;
            return this;
        }

        public TransitAnalysisBuilder majorTransits(Map<String, String> majorTransits) {
            this.majorTransits = majorTransits;
            return this;
        }

        public TransitAnalysis build() {
            TransitAnalysis analysis = new TransitAnalysis();
            analysis.setTransitDate(transitDate);
            analysis.setTransitPositions(transitPositions);
            analysis.setAspects(aspects);
            analysis.setMajorTransits(majorTransits);
            return analysis;
        }
    }

    public static class TransitAspectBuilder {
        private String transitPlanet;
        private String natalPlanet;
        private String aspectType;
        private double orb;

        public TransitAspectBuilder transitPlanet(String transitPlanet) {
            this.transitPlanet = transitPlanet;
            return this;
        }

        public TransitAspectBuilder natalPlanet(String natalPlanet) {
            this.natalPlanet = natalPlanet;
            return this;
        }

        public TransitAspectBuilder aspectType(String aspectType) {
            this.aspectType = aspectType;
            return this;
        }

        public TransitAspectBuilder orb(double orb) {
            this.orb = orb;
            return this;
        }

        public TransitAspect build() {
            TransitAspect aspect = new TransitAspect();
            aspect.setTransitPlanet(transitPlanet);
            aspect.setNatalPlanet(natalPlanet);
            aspect.setAspectType(aspectType);
            aspect.setOrb(orb);
            return aspect;
        }
    }
}
