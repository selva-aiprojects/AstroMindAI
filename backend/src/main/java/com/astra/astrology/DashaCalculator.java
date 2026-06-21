package com.astra.astrology;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DashaCalculator {

    // Vimshottari Dasha periods in years
    private static final Map<String, Integer> DASHA_PERIODS = Map.of(
            "Ketu", 7,
            "Venus", 20,
            "Sun", 6,
            "Moon", 10,
            "Mars", 7,
            "Rahu", 18,
            "Jupiter", 16,
            "Saturn", 19,
            "Mercury", 17
    );

    // Dasha lord order based on Nakshatra
    private static final String[] NAKSHATRA_DASHA_LORDS = {
            "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"
    };

    public DashaTimeline calculateDashaTimeline(LocalDate birthDate, double moonLongitude) {
        double nakshatraSize = 360.0 / 27.0;
        int nakshatraIndex = (int) (moonLongitude / nakshatraSize);
        if (nakshatraIndex < 0) nakshatraIndex = 0;
        if (nakshatraIndex > 26) nakshatraIndex = 26;

        String[] nakshatras = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira",
            "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
            "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
            "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
            "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
            "Uttara Bhadrapada", "Revati"
        };
        String moonNakshatra = nakshatras[nakshatraIndex];

        log.info("Calculating Dasha timeline for birth date: {}, moon longitude: {} ({})", birthDate, moonLongitude, moonNakshatra);

        // Get Mahadasha lord based on Moon's Nakshatra
        int lordIndex = nakshatraIndex % 9;
        String mahadashaLord = NAKSHATRA_DASHA_LORDS[lordIndex];
        
        // Calculate Mahadasha periods
        List<DashaPeriod> mahadashas = calculateMahadashas(birthDate, mahadashaLord, moonLongitude);
        
        // Calculate current active Dasha
        DashaPeriod currentDasha = getCurrentDasha(mahadashas, LocalDate.now());
        
        // Calculate Antardashas for current Mahadasha
        List<DashaPeriod> antardashas = currentDasha != null ? 
                calculateAntardashas(currentDasha.getStartDate(), currentDasha.getEndDate(), currentDasha.getLord()) : 
                new ArrayList<>();
        
        // Calculate current Antardasha
        DashaPeriod currentAntardasha = antardashas.isEmpty() ? null : 
                getCurrentDasha(antardashas, LocalDate.now());
        
        return DashaTimeline.builder()
                .mahadashas(mahadashas)
                .currentMahadasha(currentDasha)
                .antardashas(antardashas)
                .currentAntardasha(currentAntardasha)
                .build();
    }

    public DashaTimeline calculateDashaTimeline(LocalDate birthDate, String moonNakshatra) {
        int index = getNakshatraIndex(moonNakshatra);
        double estimatedLongitude = (index + 0.5) * (360.0 / 27.0);
        return calculateDashaTimeline(birthDate, estimatedLongitude);
    }

    private String getMahadashaLord(String moonNakshatra) {
        // Get Dasha lord based on Nakshatra
        int nakshatraIndex = getNakshatraIndex(moonNakshatra);
        int pada = nakshatraIndex % 9;
        return NAKSHATRA_DASHA_LORDS[pada];
    }

    private int getNakshatraIndex(String nakshatra) {
        String[] nakshatras = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira",
            "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha",
            "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati",
            "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
            "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
            "Uttara Bhadrapada", "Revati"
        };
        
        for (int i = 0; i < nakshatras.length; i++) {
            if (nakshatras[i].equalsIgnoreCase(nakshatra)) {
                return i;
            }
        }
        
        return 0; // Default to Ashwini
    }

    private List<DashaPeriod> calculateMahadashas(LocalDate birthDate, String startingLord, double moonLongitude) {
        List<DashaPeriod> mahadashas = new ArrayList<>();
        
        double nakshatraSize = 360.0 / 27.0;
        int nakshatraIndex = (int) (moonLongitude / nakshatraSize);
        if (nakshatraIndex < 0) nakshatraIndex = 0;
        if (nakshatraIndex > 26) nakshatraIndex = 26;
        
        double elapsedLongitude = moonLongitude - (nakshatraIndex * nakshatraSize);
        double fractionRemaining = 1.0 - (elapsedLongitude / nakshatraSize);
        if (fractionRemaining < 0) fractionRemaining = 0;
        if (fractionRemaining > 1) fractionRemaining = 1;
        
        int totalPeriod = DASHA_PERIODS.get(startingLord);
        double remainingYears = totalPeriod * fractionRemaining;
        
        LocalDate currentDate = birthDate;
        
        // Add first Mahadasha
        long remainingDays = (long) (remainingYears * 365.25);
        LocalDate firstDashaEndDate = birthDate.plusDays(remainingDays);
        mahadashas.add(DashaPeriod.builder()
                .lord(startingLord)
                .level("Mahadasha")
                .startDate(currentDate)
                .endDate(firstDashaEndDate)
                .years((long) remainingYears)
                .build());
        
        currentDate = firstDashaEndDate;
        
        // Add remaining Mahadashas in order
        int startIndex = getDashaLordIndex(startingLord);
        for (int i = 1; i < 9; i++) {
            int lordIndex = (startIndex + i) % 9;
            String lord = NAKSHATRA_DASHA_LORDS[lordIndex];
            int years = DASHA_PERIODS.get(lord);
            
            mahadashas.add(DashaPeriod.builder()
                    .lord(lord)
                    .level("Mahadasha")
                    .startDate(currentDate)
                    .endDate(currentDate.plusYears(years))
                    .years(years)
                    .build());
            
            currentDate = currentDate.plusYears(years);
        }
        
        return mahadashas;
    }

    private List<DashaPeriod> calculateMahadashas(LocalDate birthDate, String startingLord) {
        int index = getDashaLordIndex(startingLord);
        double estimatedLongitude = (index + 0.5) * (360.0 / 27.0);
        return calculateMahadashas(birthDate, startingLord, estimatedLongitude);
    }

    private List<DashaPeriod> calculateAntardashas(LocalDate startDate, LocalDate endDate, String mahadashaLord) {
        List<DashaPeriod> antardashas = new ArrayList<>();
        
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate currentDate = startDate;
        
        // Calculate Antardashas proportional to Mahadasha periods
        int startIndex = getDashaLordIndex(mahadashaLord);
        
        for (int i = 0; i < 9; i++) {
            int lordIndex = (startIndex + i) % 9;
            String lord = NAKSHATRA_DASHA_LORDS[lordIndex];
            int periodYears = DASHA_PERIODS.get(lord);
            int mahadashaYears = DASHA_PERIODS.get(mahadashaLord);
            
            // Calculate Antardasha period proportionally
            double proportion = (double) periodYears / 120.0; // Total Vimshottari period is 120 years
            long antardashaDays = (long) (totalDays * proportion);
            
            LocalDate antardashaEndDate = currentDate.plusDays(antardashaDays);
            
            // Don't exceed Mahadasha end date
            if (antardashaEndDate.isAfter(endDate)) {
                antardashaEndDate = endDate;
            }
            
            antardashas.add(DashaPeriod.builder()
                    .lord(lord)
                    .level("Antardasha")
                    .startDate(currentDate)
                    .endDate(antardashaEndDate)
                    .years(antardashaDays / 365)
                    .build());
            
            currentDate = antardashaEndDate;
            
            if (currentDate.isAfter(endDate) || currentDate.equals(endDate)) {
                break;
            }
        }
        
        return antardashas;
    }

    private DashaPeriod getCurrentDasha(List<DashaPeriod> dashas, LocalDate currentDate) {
        for (DashaPeriod dasha : dashas) {
            if (!currentDate.isBefore(dasha.getStartDate()) && currentDate.isBefore(dasha.getEndDate())) {
                return dasha;
            }
        }
        return null;
    }

    private int getDashaLordIndex(String lord) {
        for (int i = 0; i < NAKSHATRA_DASHA_LORDS.length; i++) {
            if (NAKSHATRA_DASHA_LORDS[i].equals(lord)) {
                return i;
            }
        }
        return 0;
    }

    @Data
    public static class DashaTimeline {
        private List<DashaPeriod> mahadashas;
        private DashaPeriod currentMahadasha;
        private List<DashaPeriod> antardashas;
        private DashaPeriod currentAntardasha;
        
        public static DashaTimelineBuilder builder() {
            return new DashaTimelineBuilder();
        }
    }

    @Data
    public static class DashaPeriod {
        private String lord;
        private String level;
        private LocalDate startDate;
        private LocalDate endDate;
        private long years;
        
        public static DashaPeriodBuilder builder() {
            return new DashaPeriodBuilder();
        }
    }

    public static class DashaTimelineBuilder {
        private List<DashaPeriod> mahadashas;
        private DashaPeriod currentMahadasha;
        private List<DashaPeriod> antardashas;
        private DashaPeriod currentAntardasha;

        public DashaTimelineBuilder mahadashas(List<DashaPeriod> mahadashas) {
            this.mahadashas = mahadashas;
            return this;
        }

        public DashaTimelineBuilder currentMahadasha(DashaPeriod currentMahadasha) {
            this.currentMahadasha = currentMahadasha;
            return this;
        }

        public DashaTimelineBuilder antardashas(List<DashaPeriod> antardashas) {
            this.antardashas = antardashas;
            return this;
        }

        public DashaTimelineBuilder currentAntardasha(DashaPeriod currentAntardasha) {
            this.currentAntardasha = currentAntardasha;
            return this;
        }

        public DashaTimeline build() {
            DashaTimeline timeline = new DashaTimeline();
            timeline.setMahadashas(mahadashas);
            timeline.setCurrentMahadasha(currentMahadasha);
            timeline.setAntardashas(antardashas);
            timeline.setCurrentAntardasha(currentAntardasha);
            return timeline;
        }
    }

    public static class DashaPeriodBuilder {
        private String lord;
        private String level;
        private LocalDate startDate;
        private LocalDate endDate;
        private long years;

        public DashaPeriodBuilder lord(String lord) {
            this.lord = lord;
            return this;
        }

        public DashaPeriodBuilder level(String level) {
            this.level = level;
            return this;
        }

        public DashaPeriodBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public DashaPeriodBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public DashaPeriodBuilder years(long years) {
            this.years = years;
            return this;
        }

        public DashaPeriod build() {
            DashaPeriod period = new DashaPeriod();
            period.setLord(lord);
            period.setLevel(level);
            period.setStartDate(startDate);
            period.setEndDate(endDate);
            period.setYears(years);
            return period;
        }
    }
}
