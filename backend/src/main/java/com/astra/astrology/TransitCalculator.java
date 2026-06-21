package com.astra.astrology;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import swisseph.SweConst;
import swisseph.SwissEph;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TransitCalculator {

    private static final String[] NAKSHATRAS = {
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira",
        "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
        "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
        "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
        "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
        "Uttara Bhadrapada", "Revati"
    };

    private static final String[] SIGNS = {
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    private static final int[] PLANET_IDS = {
        SweConst.SE_SUN, SweConst.SE_MOON, SweConst.SE_MARS, SweConst.SE_MERCURY,
        SweConst.SE_JUPITER, SweConst.SE_VENUS, SweConst.SE_SATURN,
        SweConst.SE_MEAN_NODE, 11
    };
    private static final String[] PLANET_NAMES = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu"};

    private final SwissEph swissEph;
    private final boolean libraryAvailable;

    public TransitCalculator() {
        SwissEph instance = null;
        boolean available = false;
        try {
            instance = new SwissEph();
            available = true;
        } catch (Exception e) {
            log.warn("Swiss Ephemeris not available for transit calc: {}", e.getMessage());
        }
        this.swissEph = instance;
        this.libraryAvailable = available;
    }

    public TransitAnalysis calculateTransits(LocalDate currentDate, Map<String, SwissEphemerisService.PlanetPosition> natalPositions) {
        log.info("Calculating transits for date: {}", currentDate);

        Map<String, SwissEphemerisService.PlanetPosition> transitPositions = calculateCurrentPositions(currentDate);
        Map<String, TransitAspect> aspects = calculateAspects(transitPositions, natalPositions);
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
        double julianDay = convertToJulianDay(currentDate);

        for (int i = 0; i < PLANET_IDS.length; i++) {
            if (PLANET_NAMES[i].equals("Ketu")) {
                SwissEphemerisService.PlanetPosition rahuPos = positions.get("Rahu");
                if (rahuPos != null) {
                    double ketuLongitude = (rahuPos.getLongitude() + 180) % 360;
                    positions.put("Ketu", SwissEphemerisService.PlanetPosition.builder()
                            .longitude(ketuLongitude)
                            .nakshatra(calculateNakshatra(ketuLongitude))
                            .sign(calculateSign(ketuLongitude))
                            .isRetrograde(rahuPos.isRetrograde())
                            .isCombust(false)
                            .build());
                } else {
                    positions.put("Ketu", calculatePosition(PLANET_IDS[i], julianDay));
                }
            } else {
                positions.put(PLANET_NAMES[i], calculatePosition(PLANET_IDS[i], julianDay));
            }
        }

        return positions;
    }

    private SwissEphemerisService.PlanetPosition calculatePosition(int planetId, double julianDay) {
        double longitude;
        boolean isRetrograde = false;

        if (libraryAvailable) {
            try {
                double[] xx = new double[6];
                synchronized (swissEph) {
                    swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
                    int iflag = SweConst.SEFLG_SPEED | SweConst.SEFLG_TRUEPOS | SweConst.SEFLG_SIDEREAL;
                    swissEph.swe_calc_ut(julianDay, planetId, iflag, xx, null);
                }
                longitude = xx[0];
                isRetrograde = xx[3] < 0;
            } catch (Exception e) {
                longitude = calculateSiderealApprox(planetId, julianDay);
            }
        } else {
            longitude = calculateSiderealApprox(planetId, julianDay);
        }

        if (longitude < 0) longitude += 360;
        if (longitude >= 360) longitude -= 360;

        return SwissEphemerisService.PlanetPosition.builder()
                .longitude(longitude)
                .nakshatra(calculateNakshatra(longitude))
                .sign(calculateSign(longitude))
                .isRetrograde(isRetrograde)
                .isCombust(false)
                .build();
    }

    private double calculateSiderealApprox(int planetId, double julianDay) {
        double tropical = (julianDay * (planetId + 1) * 0.9856) % 360;
        double sidereal = tropical - 24.0;
        if (sidereal < 0) sidereal += 360;
        return sidereal % 360;
    }

    private Map<String, TransitAspect> calculateAspects(
            Map<String, SwissEphemerisService.PlanetPosition> transitPositions,
            Map<String, SwissEphemerisService.PlanetPosition> natalPositions) {

        Map<String, TransitAspect> aspects = new HashMap<>();

        for (Map.Entry<String, SwissEphemerisService.PlanetPosition> transitEntry : transitPositions.entrySet()) {
            for (Map.Entry<String, SwissEphemerisService.PlanetPosition> natalEntry : natalPositions.entrySet()) {
                String key = transitEntry.getKey() + " over " + natalEntry.getKey();
                double difference = Math.abs(transitEntry.getValue().getLongitude() - natalEntry.getValue().getLongitude());
                if (difference > 180) difference = 360 - difference;

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
        SwissEphemerisService.PlanetPosition natalMoon = natalPositions.get("Moon");

        if (natalMoon != null) {
            double moonLong = natalMoon.getLongitude();

            SwissEphemerisService.PlanetPosition transitSaturn = transitPositions.get("Saturn");
            if (transitSaturn != null) {
                double diff = angularDiff(moonLong, transitSaturn.getLongitude());
                if (diff < 15) {
                    majorTransits.put("Saturn Sade Sati", "Saturn is transiting near your natal Moon - Sade Sati period may be active");
                }
            }

            SwissEphemerisService.PlanetPosition transitJupiter = transitPositions.get("Jupiter");
            if (transitJupiter != null) {
                double diff = angularDiff(moonLong, transitJupiter.getLongitude());
                if (diff < 5) {
                    majorTransits.put("Jupiter Transit", "Jupiter is conjunct your natal Moon - favorable period for growth");
                }
            }

            SwissEphemerisService.PlanetPosition transitRahu = transitPositions.get("Rahu");
            if (transitRahu != null) {
                double diff = angularDiff(moonLong, transitRahu.getLongitude());
                if (diff < 5) {
                    majorTransits.put("Rahu Transit", "Rahu is conjunct your natal Moon - transformative period");
                }
            }
        }

        return majorTransits;
    }

    private double angularDiff(double a, double b) {
        double diff = Math.abs(a - b);
        return diff > 180 ? 360 - diff : diff;
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
        double size = 360.0 / 27.0;
        return NAKSHATRAS[Math.min((int)(longitude / size), 26)];
    }

    private String calculateSign(double longitude) {
        return SIGNS[Math.min((int)(longitude / 30), 11)];
    }

    @Data
    @Builder
    public static class TransitAnalysis {
        private LocalDate transitDate;
        private Map<String, SwissEphemerisService.PlanetPosition> transitPositions;
        private Map<String, TransitAspect> aspects;
        private Map<String, String> majorTransits;
    }

    @Data
    @Builder
    public static class TransitAspect {
        private String transitPlanet;
        private String natalPlanet;
        private String aspectType;
        private double orb;
    }
}
