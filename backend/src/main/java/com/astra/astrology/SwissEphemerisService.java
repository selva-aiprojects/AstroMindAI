package com.astra.astrology;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SwissEphemerisService {

    // Planet constants (Swiss Ephemeris planet numbers)
    private static final int SUN = 0;
    private static final int MOON = 1;
    private static final int MARS = 2;
    private static final int MERCURY = 3;
    private static final int JUPITER = 4;
    private static final int VENUS = 5;
    private static final int SATURN = 6;
    private static final int RAHU = 10;  // Mean North Node
    private static final int KETU = 11;  // Mean South Node

    // Ayanamsa constants
    private static final double LAHIRI_AYANAMSA = 24.0;
    private static final double RAMAN_AYANAMSA = 22.5;
    private static final double KP_AYANAMSA = 23.5;
    private static final double FAGAN_BRADLEY_AYANAMSA = 24.5;

    public BirthChart calculateBirthChart(LocalDate birthDate, LocalTime birthTime, 
                                          double latitude, double longitude, String timezone, String ayanamsa) {
        log.info("Calculating birth chart for date: {}, time: {}, location: {}, {}, ayanamsa: {}", 
                birthDate, birthTime, latitude, longitude, ayanamsa);

        // Convert to Julian Day
        double julianDay = convertToJulianDay(birthDate, birthTime, timezone);
        
        // Calculate planetary positions
        Map<String, PlanetPosition> planetaryPositions = calculatePlanetaryPositions(julianDay, ayanamsa);
        
        // Calculate ascendant (Lagna)
        double ascendant = calculateAscendant(julianDay, latitude, longitude, ayanamsa);
        
        // Calculate house placements
        Map<String, Integer> housePlacements = calculateHousePlacements(planetaryPositions, ascendant);
        
        // Determine functional nature
        Map<String, String> functionalNature = determineFunctionalNature(ascendant);
        
        return BirthChart.builder()
                .julianDay(julianDay)
                .planetaryPositions(planetaryPositions)
                .ascendant(ascendant)
                .housePlacements(housePlacements)
                .functionalNature(functionalNature)
                .ayanamsa(ayanamsa)
                .build();
    }

    private double convertToJulianDay(LocalDate birthDate, LocalTime birthTime, String timezone) {
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(birthDate, birthTime, zoneId);
            
            // Simplified Julian Day calculation
            // In production, use proper astronomical library
            int year = zonedDateTime.getYear();
            int month = zonedDateTime.getMonthValue();
            int day = zonedDateTime.getDayOfMonth();
            double hour = zonedDateTime.getHour() + zonedDateTime.getMinute() / 60.0 + zonedDateTime.getSecond() / 3600.0;
            
            // Julian Day formula
            if (month <= 2) {
                year -= 1;
                month += 12;
            }
            
            int a = year / 100;
            int b = 2 - a + a / 4;
            
            double jd = (int)(365.25 * (year + 4716)) + (int)(30.6001 * (month + 1)) + day + b - 1524.5 + hour / 24.0;
            
            return jd;
        } catch (Exception e) {
            log.error("Error converting to Julian Day", e);
            throw new RuntimeException("Failed to calculate Julian Day");
        }
    }

    private Map<String, PlanetPosition> calculatePlanetaryPositions(double julianDay, String ayanamsa) {
        Map<String, PlanetPosition> positions = new HashMap<>();
        
        // Get Ayanamsa correction
        double ayanamsaCorrection = getAyanamsaCorrection(ayanamsa);
        
        // Calculate positions for each planet
        // In production, use actual Swiss Ephemeris library
        positions.put("Sun", calculatePlanetPosition(SUN, julianDay, ayanamsaCorrection));
        positions.put("Moon", calculatePlanetPosition(MOON, julianDay, ayanamsaCorrection));
        positions.put("Mars", calculatePlanetPosition(MARS, julianDay, ayanamsaCorrection));
        positions.put("Mercury", calculatePlanetPosition(MERCURY, julianDay, ayanamsaCorrection));
        positions.put("Jupiter", calculatePlanetPosition(JUPITER, julianDay, ayanamsaCorrection));
        positions.put("Venus", calculatePlanetPosition(VENUS, julianDay, ayanamsaCorrection));
        positions.put("Saturn", calculatePlanetPosition(SATURN, julianDay, ayanamsaCorrection));
        positions.put("Rahu", calculatePlanetPosition(RAHU, julianDay, ayanamsaCorrection));
        positions.put("Ketu", calculatePlanetPosition(KETU, julianDay, ayanamsaCorrection));
        
        return positions;
    }

    private PlanetPosition calculatePlanetPosition(int planet, double julianDay, double ayanamsaCorrection) {
        // Placeholder calculation - in production, use Swiss Ephemeris library
        // This is a simplified approximation for development
        
        double tropicalLongitude = calculateTropicalPosition(planet, julianDay);
        double siderealLongitude = tropicalLongitude - ayanamsaCorrection;
        
        // Normalize to 0-360 degrees
        if (siderealLongitude < 0) {
            siderealLongitude += 360;
        }
        if (siderealLongitude >= 360) {
            siderealLongitude -= 360;
        }
        
        // Calculate nakshatra
        String nakshatra = calculateNakshatra(siderealLongitude);
        
        // Calculate sign
        String sign = calculateSign(siderealLongitude);
        
        // Check retrograde (simplified)
        boolean isRetrograde = checkRetrograde(planet, julianDay);
        
        // Check combustion (simplified)
        boolean isCombust = checkCombustion(planet, siderealLongitude);
        
        return PlanetPosition.builder()
                .longitude(siderealLongitude)
                .nakshatra(nakshatra)
                .sign(sign)
                .isRetrograde(isRetrograde)
                .isCombust(isCombust)
                .build();
    }

    private double calculateTropicalPosition(int planet, double julianDay) {
        // Placeholder - in production, use Swiss Ephemeris
        // This is a simplified approximation
        double basePosition = (julianDay * (planet + 1) * 0.9856) % 360;
        return basePosition < 0 ? basePosition + 360 : basePosition;
    }

    private double calculateAscendant(double julianDay, double latitude, double longitude, String ayanamsa) {
        // Placeholder calculation - in production, use proper formula
        double ayanamsaCorrection = getAyanamsaCorrection(ayanamsa);
        
        // Simplified ascendant calculation
        double tropicalAscendant = (julianDay * 0.9856 + longitude) % 360;
        double siderealAscendant = tropicalAscendant - ayanamsaCorrection;
        
        if (siderealAscendant < 0) {
            siderealAscendant += 360;
        }
        if (siderealAscendant >= 360) {
            siderealAscendant -= 360;
        }
        
        return siderealAscendant;
    }

    private Map<String, Integer> calculateHousePlacements(Map<String, PlanetPosition> planetaryPositions, double ascendant) {
        Map<String, Integer> housePlacements = new HashMap<>();
        
        for (Map.Entry<String, PlanetPosition> entry : planetaryPositions.entrySet()) {
            double planetLongitude = entry.getValue().getLongitude();
            int house = calculateHouse(planetLongitude, ascendant);
            housePlacements.put(entry.getKey(), house);
        }
        
        return housePlacements;
    }

    private int calculateHouse(double planetLongitude, double ascendant) {
        // Simplified house calculation (equal house system)
        // In production, use Placidus or other house systems
        double difference = planetLongitude - ascendant;
        if (difference < 0) {
            difference += 360;
        }
        
        int house = (int)(difference / 30) + 1;
        return house > 12 ? house - 12 : house;
    }

    private Map<String, String> determineFunctionalNature(double ascendant) {
        Map<String, String> functionalNature = new HashMap<>();
        
        // Determine ascendant sign
        String ascendantSign = calculateSign(ascendant);
        
        // Simplified functional benefic/malefic determination
        // In production, use proper Vedic astrology rules
        switch (ascendantSign) {
            case "Aries":
                functionalNature.put("Jupiter", "Benefic");
                functionalNature.put("Sun", "Malefic");
                functionalNature.put("Saturn", "Malefic");
                functionalNature.put("Mars", "Benefic");
                functionalNature.put("Venus", "Benefic");
                functionalNature.put("Mercury", "Neutral");
                functionalNature.put("Moon", "Benefic");
                break;
            case "Taurus":
                functionalNature.put("Saturn", "Benefic");
                functionalNature.put("Venus", "Benefic");
                functionalNature.put("Jupiter", "Malefic");
                functionalNature.put("Mercury", "Benefic");
                functionalNature.put("Mars", "Malefic");
                functionalNature.put("Sun", "Malefic");
                functionalNature.put("Moon", "Neutral");
                break;
            // Add other signs...
            default:
                functionalNature.put("Jupiter", "Benefic");
                functionalNature.put("Venus", "Benefic");
                functionalNature.put("Saturn", "Malefic");
                functionalNature.put("Mars", "Malefic");
                functionalNature.put("Sun", "Malefic");
                functionalNature.put("Mercury", "Neutral");
                functionalNature.put("Moon", "Neutral");
        }
        
        return functionalNature;
    }

    private double getAyanamsaCorrection(String ayanamsa) {
        return switch (ayanamsa.toUpperCase()) {
            case "LAHIRI" -> LAHIRI_AYANAMSA;
            case "RAMAN" -> RAMAN_AYANAMSA;
            case "KP" -> KP_AYANAMSA;
            case "FAGAN_BRADLEY" -> FAGAN_BRADLEY_AYANAMSA;
            default -> LAHIRI_AYANAMSA;
        };
    }

    private String calculateNakshatra(double longitude) {
        // 27 Nakshatras, each 13°20' (13.333... degrees)
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
        // 12 signs, each 30 degrees
        int signIndex = (int)(longitude / 30);
        
        String[] signs = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
        };
        
        return signs[signIndex];
    }

    private boolean checkRetrograde(int planet, double julianDay) {
        // Simplified retrograde check
        // In production, use actual Swiss Ephemeris data
        return switch (planet) {
            case MERCURY, VENUS, MARS, JUPITER, SATURN -> (julianDay % 100) < 50;
            default -> false;
        };
    }

    private boolean checkCombustion(int planet, double planetLongitude) {
        // Simplified combustion check (within 15 degrees of Sun)
        // In production, calculate actual Sun position
        double sunPosition = 100.0; // Placeholder
        double difference = Math.abs(planetLongitude - sunPosition);
        return difference < 15 || difference > 345;
    }

    @Data
    public static class BirthChart {
        private double julianDay;
        private Map<String, PlanetPosition> planetaryPositions;
        private double ascendant;
        private Map<String, Integer> housePlacements;
        private Map<String, String> functionalNature;
        private String ayanamsa;
        
        public static BirthChartBuilder builder() {
            return new BirthChartBuilder();
        }
    }

    @Data
    public static class PlanetPosition {
        private double longitude;
        private String nakshatra;
        private String sign;
        private boolean isRetrograde;
        private boolean isCombust;
        
        public static PlanetPositionBuilder builder() {
            return new PlanetPositionBuilder();
        }
    }

    public static class BirthChartBuilder {
        private double julianDay;
        private Map<String, PlanetPosition> planetaryPositions;
        private double ascendant;
        private Map<String, Integer> housePlacements;
        private Map<String, String> functionalNature;
        private String ayanamsa;

        public BirthChartBuilder julianDay(double julianDay) {
            this.julianDay = julianDay;
            return this;
        }

        public BirthChartBuilder planetaryPositions(Map<String, PlanetPosition> planetaryPositions) {
            this.planetaryPositions = planetaryPositions;
            return this;
        }

        public BirthChartBuilder ascendant(double ascendant) {
            this.ascendant = ascendant;
            return this;
        }

        public BirthChartBuilder housePlacements(Map<String, Integer> housePlacements) {
            this.housePlacements = housePlacements;
            return this;
        }

        public BirthChartBuilder functionalNature(Map<String, String> functionalNature) {
            this.functionalNature = functionalNature;
            return this;
        }

        public BirthChartBuilder ayanamsa(String ayanamsa) {
            this.ayanamsa = ayanamsa;
            return this;
        }

        public BirthChart build() {
            BirthChart chart = new BirthChart();
            chart.setJulianDay(julianDay);
            chart.setPlanetaryPositions(planetaryPositions);
            chart.setAscendant(ascendant);
            chart.setHousePlacements(housePlacements);
            chart.setFunctionalNature(functionalNature);
            chart.setAyanamsa(ayanamsa);
            return chart;
        }
    }

    public static class PlanetPositionBuilder {
        private double longitude;
        private String nakshatra;
        private String sign;
        private boolean isRetrograde;
        private boolean isCombust;

        public PlanetPositionBuilder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public PlanetPositionBuilder nakshatra(String nakshatra) {
            this.nakshatra = nakshatra;
            return this;
        }

        public PlanetPositionBuilder sign(String sign) {
            this.sign = sign;
            return this;
        }

        public PlanetPositionBuilder isRetrograde(boolean isRetrograde) {
            this.isRetrograde = isRetrograde;
            return this;
        }

        public PlanetPositionBuilder isCombust(boolean isCombust) {
            this.isCombust = isCombust;
            return this;
        }

        public PlanetPosition build() {
            PlanetPosition position = new PlanetPosition();
            position.setLongitude(longitude);
            position.setNakshatra(nakshatra);
            position.setSign(sign);
            position.setRetrograde(isRetrograde);
            position.setCombust(isCombust);
            return position;
        }
    }
}
