package com.astra.astrology;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import swisseph.SweConst;
import swisseph.SwissEph;

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
    private static final int SUN = SweConst.SE_SUN;
    private static final int MOON = SweConst.SE_MOON;
    private static final int MARS = SweConst.SE_MARS;
    private static final int MERCURY = SweConst.SE_MERCURY;
    private static final int JUPITER = SweConst.SE_JUPITER;
    private static final int VENUS = SweConst.SE_VENUS;
    private static final int SATURN = SweConst.SE_SATURN;
    private static final int RAHU = SweConst.SE_MEAN_NODE;
    private static final int KETU = 11;

    private static final int[] PLANET_IDS = {SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN, RAHU, KETU};
    private static final String[] PLANET_NAMES = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu"};

    private static final String[] SIGNS = {
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    private static final String[] NAKSHATRAS = {
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira",
        "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
        "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
        "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
        "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
        "Uttara Bhadrapada", "Revati"
    };

    private final SwissEph swissEph;
    private final boolean libraryAvailable;

    @Value("${swisseph.ephemeris.path:/ephemeris}")
    private String ephemerisPath;

    public SwissEphemerisService() {
        SwissEph instance = null;
        boolean available = false;
        try {
            instance = new SwissEph();
            instance.swe_set_ephe_path(ephemerisPath);
            available = true;
            log.info("Swiss Ephemeris library loaded successfully");
        } catch (Exception e) {
            log.warn("Swiss Ephemeris native library not available, using simplified calculations: {}", e.getMessage());
        }
        this.swissEph = instance;
        this.libraryAvailable = available;
    }

    public boolean isLibraryAvailable() {
        return libraryAvailable;
    }

    public synchronized BirthChart calculateBirthChart(LocalDate birthDate, LocalTime birthTime,
                                          double latitude, double longitude, String timezone, String ayanamsa) {
        log.info("Calculating birth chart for date: {}, time: {}, location: {}, {}, ayanamsa: {}",
                birthDate, birthTime, latitude, longitude, ayanamsa);

        double julianDay = convertToJulianDay(birthDate, birthTime, timezone);
        
        double ayanamsaVal = 24.0;
        if (libraryAvailable) {
            int sidMode = getSiderealMode(ayanamsa);
            swissEph.swe_set_sid_mode(sidMode, 0, 0);
            ayanamsaVal = swissEph.swe_get_ayanamsa_ut(julianDay);
        } else {
            ayanamsaVal = getAyanamsaCorrection(ayanamsa);
        }

        Map<String, PlanetPosition> planetaryPositions = calculatePlanetaryPositions(julianDay, ayanamsaVal);
        double ascendant = calculateAscendant(julianDay, latitude, longitude, ayanamsaVal);
        Map<String, Integer> housePlacements = calculateHousePlacements(planetaryPositions, ascendant);
        Map<String, String> functionalNature = determineFunctionalNature(ascendant);

        return BirthChart.builder()
                .julianDay(julianDay)
                .planetaryPositions(planetaryPositions)
                .ascendant(ascendant)
                .housePlacements(housePlacements)
                .functionalNature(functionalNature)
                .ayanamsa(ayanamsa)
                .ayanamsaValue(ayanamsaVal)
                .build();
    }

    private double convertToJulianDay(LocalDate birthDate, LocalTime birthTime, String timezone) {
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(birthDate, birthTime, zoneId);
            ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

            int year = utcDateTime.getYear();
            int month = utcDateTime.getMonthValue();
            int day = utcDateTime.getDayOfMonth();
            double hour = utcDateTime.getHour() + utcDateTime.getMinute() / 60.0 + utcDateTime.getSecond() / 3600.0;

            if (month <= 2) {
                year -= 1;
                month += 12;
            }

            int a = year / 100;
            int b = 2 - a + a / 4;

            return (int)(365.25 * (year + 4716)) + (int)(30.6001 * (month + 1)) + day + b - 1524.5 + hour / 24.0;
        } catch (Exception e) {
            log.error("Error converting to Julian Day", e);
            throw new RuntimeException("Failed to calculate Julian Day");
        }
    }

    private Map<String, PlanetPosition> calculatePlanetaryPositions(double julianDay, double ayanamsaCorrection) {
        Map<String, PlanetPosition> positions = new HashMap<>();

        for (int i = 0; i < PLANET_IDS.length; i++) {
            if (PLANET_NAMES[i].equals("Ketu")) {
                PlanetPosition rahuPos = positions.get("Rahu");
                if (rahuPos != null) {
                    double ketuLongitude = (rahuPos.getLongitude() + 180) % 360;
                    positions.put("Ketu", PlanetPosition.builder()
                            .longitude(ketuLongitude)
                            .nakshatra(calculateNakshatra(ketuLongitude))
                            .sign(calculateSign(ketuLongitude))
                            .isRetrograde(rahuPos.isRetrograde())
                            .isCombust(false)
                            .build());
                } else {
                    positions.put("Ketu", calculatePlanetPosition(PLANET_IDS[i], julianDay, ayanamsaCorrection));
                }
            } else {
                positions.put(PLANET_NAMES[i], calculatePlanetPosition(PLANET_IDS[i], julianDay, ayanamsaCorrection));
            }
        }

        // Set combustion based on proximity to the Sun
        PlanetPosition sunPos = positions.get("Sun");
        if (sunPos != null) {
            double sunLong = sunPos.getLongitude();
            for (Map.Entry<String, PlanetPosition> entry : positions.entrySet()) {
                String planetName = entry.getKey();
                if (planetName.equals("Sun") || planetName.equals("Rahu") || planetName.equals("Ketu")) {
                    continue;
                }
                PlanetPosition pos = entry.getValue();
                boolean isCombust = checkCombustion(sunLong, pos.getLongitude());
                pos.setCombust(isCombust);
            }
        }

        return positions;
    }

    private PlanetPosition calculatePlanetPosition(int planetId, double julianDay, double ayanamsaCorrection) {
        double longitude;
        boolean isRetrograde = false;

        if (libraryAvailable) {
            try {
                double[] xx = new double[6];
                int iflag = SweConst.SEFLG_SPEED | SweConst.SEFLG_TRUEPOS | SweConst.SEFLG_SIDEREAL;
                int result = swissEph.swe_calc_ut(julianDay, planetId, iflag, xx, null);

                if (result >= 0) {
                    longitude = xx[0];
                    isRetrograde = xx[3] < 0;
                } else {
                    longitude = calculateSiderealPositionApprox(planetId, julianDay, ayanamsaCorrection);
                }
            } catch (Exception e) {
                log.warn("Swiss Ephemeris calculation failed for planet {}, using approximation", planetId);
                longitude = calculateSiderealPositionApprox(planetId, julianDay, ayanamsaCorrection);
            }
        } else {
            longitude = calculateSiderealPositionApprox(planetId, julianDay, ayanamsaCorrection);
        }

        if (longitude < 0) longitude += 360;
        if (longitude >= 360) longitude -= 360;

        return PlanetPosition.builder()
                .longitude(longitude)
                .nakshatra(calculateNakshatra(longitude))
                .sign(calculateSign(longitude))
                .isRetrograde(isRetrograde)
                .isCombust(false)
                .build();
    }

    private double calculateSiderealPositionApprox(int planetId, double julianDay, double ayanamsaCorrection) {
        double tropicalLong = calculateTropicalPositionApprox(planetId, julianDay);
        double siderealLong = tropicalLong - ayanamsaCorrection;
        if (siderealLong < 0) siderealLong += 360;
        return siderealLong % 360;
    }

    private double calculateTropicalPositionApprox(int planetId, double julianDay) {
        double daysSinceJ2000 = julianDay - 2451545.0;
        double meanMotion;

        switch (planetId) {
            case SweConst.SE_SUN: meanMotion = 0.9856; break;
            case SweConst.SE_MOON: meanMotion = 13.176; break;
            case SweConst.SE_MERCURY: meanMotion = 4.092; break;
            case SweConst.SE_VENUS: meanMotion = 1.602; break;
            case SweConst.SE_MARS: meanMotion = 0.524; break;
            case SweConst.SE_JUPITER: meanMotion = 0.083; break;
            case SweConst.SE_SATURN: meanMotion = 0.033; break;
            default: meanMotion = 0.9856; break;
        }

        double basePosition = (daysSinceJ2000 * meanMotion) % 360;
        double[] epochs = {2440587.5, 2441317.5, 2442047.5, 2442777.5, 2443507.5, 2444237.5, 2444967.5};
        double[] offsets = {0, 45, 90, 135, 180, 225, 270};

        int epochIdx = Math.abs((int)(julianDay / 365.25)) % epochs.length;
        basePosition += offsets[epochIdx];

        return basePosition < 0 ? basePosition + 360 : basePosition % 360;
    }

    private double calculateAscendant(double julianDay, double latitude, double longitude, double ayanamsaCorrection) {
        if (libraryAvailable) {
            try {
                double[] cusps = new double[13];
                double[] ascmc = new double[10];
                int result = swissEph.swe_houses(julianDay, SweConst.SEFLG_SIDEREAL, latitude, longitude, 'P', cusps, ascmc);
                if (result >= 0) {
                    double siderealAscendant = ascmc[0];
                    if (siderealAscendant < 0) siderealAscendant += 360;
                    return siderealAscendant % 360;
                }
            } catch (Exception e) {
                log.warn("House calculation failed, using approximation");
            }
        }

        double siderealAsc = (julianDay * 0.9856 + longitude) % 360 - ayanamsaCorrection;
        if (siderealAsc < 0) siderealAsc += 360;
        return siderealAsc % 360;
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
        int planetSign = (int) (planetLongitude / 30);
        int ascendantSign = (int) (ascendant / 30);
        int house = planetSign - ascendantSign + 1;
        if (house <= 0) house += 12;
        return house;
    }

    private Map<String, String> determineFunctionalNature(double ascendant) {
        Map<String, String> functionalNature = new HashMap<>();
        String ascendantSign = calculateSign(ascendant);

        switch (ascendantSign) {
            case "Aries":
                functionalNature.put("Jupiter", "Benefic"); functionalNature.put("Sun", "Malefic");
                functionalNature.put("Saturn", "Malefic"); functionalNature.put("Mars", "Benefic");
                functionalNature.put("Venus", "Benefic"); functionalNature.put("Mercury", "Neutral");
                functionalNature.put("Moon", "Benefic");
                break;
            case "Taurus":
                functionalNature.put("Saturn", "Benefic"); functionalNature.put("Venus", "Benefic");
                functionalNature.put("Jupiter", "Malefic"); functionalNature.put("Mercury", "Benefic");
                functionalNature.put("Mars", "Malefic"); functionalNature.put("Sun", "Malefic");
                functionalNature.put("Moon", "Neutral");
                break;
            case "Gemini":
                functionalNature.put("Mercury", "Benefic"); functionalNature.put("Venus", "Benefic");
                functionalNature.put("Jupiter", "Neutral"); functionalNature.put("Saturn", "Malefic");
                functionalNature.put("Mars", "Malefic"); functionalNature.put("Sun", "Malefic");
                functionalNature.put("Moon", "Benefic");
                break;
            case "Cancer":
                functionalNature.put("Moon", "Benefic"); functionalNature.put("Jupiter", "Benefic");
                functionalNature.put("Mars", "Malefic"); functionalNature.put("Saturn", "Malefic");
                functionalNature.put("Venus", "Benefic"); functionalNature.put("Mercury", "Neutral");
                functionalNature.put("Sun", "Malefic");
                break;
            case "Leo":
                functionalNature.put("Sun", "Benefic"); functionalNature.put("Mars", "Benefic");
                functionalNature.put("Jupiter", "Benefic"); functionalNature.put("Saturn", "Malefic");
                functionalNature.put("Venus", "Malefic"); functionalNature.put("Mercury", "Neutral");
                functionalNature.put("Moon", "Malefic");
                break;
            case "Virgo":
                functionalNature.put("Mercury", "Benefic"); functionalNature.put("Venus", "Benefic");
                functionalNature.put("Sun", "Malefic"); functionalNature.put("Saturn", "Neutral");
                functionalNature.put("Mars", "Malefic"); functionalNature.put("Jupiter", "Benefic");
                functionalNature.put("Moon", "Benefic");
                break;
            case "Libra":
                functionalNature.put("Venus", "Benefic"); functionalNature.put("Saturn", "Benefic");
                functionalNature.put("Jupiter", "Malefic"); functionalNature.put("Mars", "Neutral");
                functionalNature.put("Sun", "Malefic"); functionalNature.put("Mercury", "Benefic");
                functionalNature.put("Moon", "Malefic");
                break;
            case "Scorpio":
                functionalNature.put("Mars", "Benefic"); functionalNature.put("Jupiter", "Benefic");
                functionalNature.put("Saturn", "Malefic"); functionalNature.put("Venus", "Malefic");
                functionalNature.put("Mercury", "Neutral"); functionalNature.put("Sun", "Malefic");
                functionalNature.put("Moon", "Benefic");
                break;
            case "Sagittarius":
                functionalNature.put("Jupiter", "Benefic"); functionalNature.put("Sun", "Benefic");
                functionalNature.put("Mars", "Benefic"); functionalNature.put("Saturn", "Malefic");
                functionalNature.put("Venus", "Malefic"); functionalNature.put("Mercury", "Neutral");
                functionalNature.put("Moon", "Neutral");
                break;
            case "Capricorn":
                functionalNature.put("Saturn", "Benefic"); functionalNature.put("Venus", "Benefic");
                functionalNature.put("Jupiter", "Malefic"); functionalNature.put("Mars", "Neutral");
                functionalNature.put("Mercury", "Benefic"); functionalNature.put("Sun", "Malefic");
                functionalNature.put("Moon", "Malefic");
                break;
            case "Aquarius":
                functionalNature.put("Saturn", "Benefic"); functionalNature.put("Rahu", "Benefic");
                functionalNature.put("Jupiter", "Malefic"); functionalNature.put("Mars", "Neutral");
                functionalNature.put("Venus", "Benefic"); functionalNature.put("Mercury", "Benefic");
                functionalNature.put("Sun", "Malefic"); functionalNature.put("Moon", "Malefic");
                break;
            case "Pisces":
                functionalNature.put("Jupiter", "Benefic"); functionalNature.put("Venus", "Benefic");
                functionalNature.put("Saturn", "Malefic"); functionalNature.put("Mars", "Malefic");
                functionalNature.put("Mercury", "Neutral"); functionalNature.put("Sun", "Benefic");
                functionalNature.put("Moon", "Benefic");
                break;
            default:
                functionalNature.put("Jupiter", "Benefic"); functionalNature.put("Venus", "Benefic");
                functionalNature.put("Saturn", "Malefic"); functionalNature.put("Mars", "Malefic");
                functionalNature.put("Sun", "Malefic"); functionalNature.put("Mercury", "Neutral");
                functionalNature.put("Moon", "Neutral");
        }

        return functionalNature;
    }

    private int getSiderealMode(String ayanamsa) {
        return switch (ayanamsa.toUpperCase()) {
            case "LAHIRI" -> SweConst.SE_SIDM_LAHIRI;
            case "RAMAN" -> SweConst.SE_SIDM_RAMAN;
            case "KP" -> SweConst.SE_SIDM_KRISHNAMURTI;
            case "FAGAN_BRADLEY" -> SweConst.SE_SIDM_FAGAN_BRADLEY;
            default -> SweConst.SE_SIDM_LAHIRI;
        };
    }

    private double getAyanamsaCorrection(String ayanamsa) {
        return switch (ayanamsa.toUpperCase()) {
            case "LAHIRI" -> 24.0;
            case "RAMAN" -> 22.5;
            case "KP" -> 23.5;
            case "FAGAN_BRADLEY" -> 24.5;
            default -> 24.0;
        };
    }

    private String calculateNakshatra(double longitude) {
        double nakshatraSize = 360.0 / 27.0;
        int index = (int)(longitude / nakshatraSize);
        return NAKSHATRAS[Math.min(index, 26)];
    }

    private String calculateSign(double longitude) {
        int index = (int)(longitude / 30);
        return SIGNS[Math.min(index, 11)];
    }

    private boolean checkCombustion(double sunLongitude, double planetLongitude) {
        double diff = Math.abs(sunLongitude - planetLongitude);
        return diff < 15 || diff > 345;
    }

    @Data
    @Builder
    public static class BirthChart {
        private double julianDay;
        private Map<String, PlanetPosition> planetaryPositions;
        private double ascendant;
        private Map<String, Integer> housePlacements;
        private Map<String, String> functionalNature;
        private String ayanamsa;
        private double ayanamsaValue;
    }

    @Data
    @Builder
    public static class PlanetPosition {
        private double longitude;
        private String nakshatra;
        private String sign;
        private boolean isRetrograde;
        private boolean isCombust;
    }
}
