package com.astra;

import com.astra.model.BirthProfile;
import com.astra.model.User;
import com.astra.repository.UserRepository;
import com.astra.security.AuthenticationService;
import com.astra.service.BirthProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BirthProfileService birthProfileService;

    @Autowired
    private com.astra.astrology.SwissEphemerisService swissEphemerisService;

    @Test
    public void testDemoAuthenticationAndProfileCreation() {
        try {
            String token = authenticationService.authenticateDemo();
            assertNotNull(token);

            Optional<User> userOpt = userRepository.findByEmail("demo@astromindai.app");
            assertTrue(userOpt.isPresent(), "Demo user should be present in DB");

            User user = userOpt.get();
            Optional<BirthProfile> profileOpt = birthProfileService.getBirthProfileByUserId(user.getUserId());
            assertTrue(profileOpt.isPresent(), "Demo birth profile should be created");
            
            BirthProfile profile = profileOpt.get();
            assertEquals("Demo User", profile.getFullName());
            assertEquals("Asia/Kolkata", profile.getTimezone());

            // Test calculateBirthChart directly
            com.astra.astrology.SwissEphemerisService.BirthChart chart = 
                swissEphemerisService.calculateBirthChart(
                    profile.getBirthDate(),
                    profile.getBirthTime(),
                    profile.getBirthLatitude().doubleValue(),
                    profile.getBirthLongitude().doubleValue(),
                    profile.getTimezone(),
                    profile.getAyanamsa()
                );
            assertNotNull(chart, "Birth chart should be successfully calculated");
            assertNotNull(chart.getPlanetaryPositions(), "Planetary positions should not be null");
            assertTrue(chart.getPlanetaryPositions().containsKey("Sun"), "Planetary positions should contain Sun");
            assertTrue(chart.getPlanetaryPositions().containsKey("Moon"), "Planetary positions should contain Moon");
            assertTrue(chart.getPlanetaryPositions().containsKey("Ketu"), "Planetary positions should contain Ketu");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    public void testSwissEphConstants() {
        try {
            System.out.println("=== SWISS EPH METHOD SIGNATURES ===");
            for (var m : swisseph.SwissEph.class.getDeclaredMethods()) {
                if (m.getName().startsWith("swe_houses")) {
                    System.out.print(m.getName() + "(");
                    for (var p : m.getParameterTypes()) {
                        System.out.print(p.getSimpleName() + ", ");
                    }
                    System.out.println(")");
                }
            }
            System.out.println("==================================");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed: " + e.getMessage());
        }
    }
}
