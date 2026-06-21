package com.astra.ai.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {

    private final ChromaService chromaService;

    public void initializeKnowledgeBase() {
        log.info("Initializing ASTRA knowledge base...");

        createCollectionIfNotExists("vedic_texts");
        createCollectionIfNotExists("remedies");
        createCollectionIfNotExists("user_memory");

        seedVedicTexts();
        seedRemedies();

        log.info("Knowledge base initialization completed");
    }

    private void createCollectionIfNotExists(String collectionName) {
        List<Map<String, Object>> collections = chromaService.getCollections();
        boolean exists = collections.stream()
                .anyMatch(col -> collectionName.equals(col.get("name")));

        if (!exists) {
            chromaService.createCollection(collectionName);
            log.info("Created collection: {}", collectionName);
        } else {
            log.info("Collection already exists: {}", collectionName);
        }
    }

    private void seedVedicTexts() {
        List<String> texts = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();

        texts.add("Parashara Hora Shastra: The foundational text of Vedic astrology written by Sage Parashara. It describes the 9 planets (Navagraha), 12 houses (Bhavas), 12 signs (Rashis), 27 nakshatras, and various yogas. Key concepts include Arudha pada, Upapada, and various divisional charts (Vargas).");
        metadatas.add(Map.of("source", "Parashara", "category", "vedic_text", "type", "classical"));

        texts.add("Brihat Jataka: Written by Varahamihira, this text covers planetary characteristics (Graha Karakatva), house meanings (Bhava Phala), yogas, and predictions based on planetary placements. It includes the famous Drekkana (D3) and Navamsha (D9) interpretations.");
        metadatas.add(Map.of("source", "Varahamihira", "category", "vedic_text", "type", "classical"));

        texts.add("Jaimini Sutras: Jaimini system uses Chara Karakas (significators based on planetary degrees) instead of the fixed Bhavat Bhavam system. Key concepts include Pada (Arudha), Karakamsha, and special Jaimini yogas. The Dara karaka relates to marriage, Gnati karaka to family.");
        metadatas.add(Map.of("source", "Jaimini", "category", "vedic_text", "type", "classical"));

        texts.add("KP Krishnamurti Paddhati: A revolutionary system by K.S. Krishnamurti using sub-lord analysis (significator system). It divides each nakshatra into 9 sub-lords using the Vimshottari Dasha sequence for precise event timing. The 4-step theory analyzes significators.");
        metadatas.add(Map.of("source", "KP", "category", "vedic_text", "type", "modern"));

        texts.add("Nadi Astrology: An ancient oracle-based system where predictions are recorded on palm leaves. It focuses on Nadi yogas formed by specific planetary combinations. The 27 nakshatra padas form the basis of Nadi analysis.");
        metadatas.add(Map.of("source", "Nadi", "category", "vedic_text", "type", "classical"));

        texts.add("Vimshottari Dasha: The 120-year planetary cycle system based on Moon's nakshatra at birth. Each planet rules a specific period: Ketu 7, Venus 20, Sun 6, Moon 10, Mars 7, Rahu 18, Jupiter 16, Saturn 19, Mercury 17 years. It is the most widely used dasha system.");
        metadatas.add(Map.of("source", "Parashara", "category", "vedic_text", "type", "timing"));

        texts.add("Shadbala: The six-fold strength system for evaluating planetary power. Includes Sthana Bala (positional), Dig Bala (directional), Kala Bala (temporal), Cheshta Bala (motional), Naisargika Bala (natural), and Drik Bala (aspectual) strengths.");
        metadatas.add(Map.of("source", "Parashara", "category", "vedic_text", "type", "strength"));

        texts.add("Ashtakavarga: A unique system by Parashara using a points-based method to evaluate planetary strengths within each sign and house. Each planet contributes a certain number of bindus (points) to each house, indicating favorable and unfavorable areas.");
        metadatas.add(Map.of("source", "Parashara", "category", "vedic_text", "type", "strength"));

        texts.add("Muhurtha: The science of electional astrology for choosing auspicious times for events. Key factors include: Tithi (lunar day), Vaara (weekday), Nakshatra, Yoga, Karana (Panchanga elements), and the placement of benefic and malefic planets in specific houses.");
        metadatas.add(Map.of("source", "Muhurtha", "category", "vedic_text", "type", "electional"));

        texts.add("Hora (D1-D60): Divisional charts provide specialized insights. D1 Rasi (physical), D2 Hora (wealth), D3 Drekkana (siblings/courage), D9 Navamsha (marriage/destiny), D10 Dasamsha (career), D12 Dwadasamsha (parents), D20 Vimsamsi (spiritual), D24 Siddhamsha (education), D27 Nakshatramsa (strength), D60 Shastiamsa (karma).");
        metadatas.add(Map.of("source", "Parashara", "category", "vedic_text", "type", "divisional"));

        texts.add("Yoga Formations: Raj Yogas (planets in kendras/trikonas), Dhana Yogas (wealth from 2nd/11th lords), Vipreet Raj Yogas (malefics in 6/8/12), Nabhasa Yogas (constellation-based), and special combinations like Gaja Kesari (Jupiter-Moon), Budha-Aditya (Sun-Mercury).");
        metadatas.add(Map.of("source", "Various", "category", "vedic_text", "type", "yoga"));

        texts.add("Arudha Pada System: Pada (reflection) of a house shows how it manifests in the material world. Lagnapada (P1) shows self-image, Upapada (UL) shows marriage, A4 shows happiness, A5 shows children, A9 shows luck, A10 shows career, A12 shows expenses and liberation.");
        metadatas.add(Map.of("source", "Parashara/Jaimini", "category", "vedic_text", "type", "pada"));

        chromaService.addDocuments("vedic_texts", texts, metadatas);
        log.info("Seeded {} Vedic texts", texts.size());
    }

    private void seedRemedies() {
        List<String> remedies = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();

        remedies.add("Sun Remedy: Worship Lord Shiva, offer water to the Sun daily at sunrise (Arghya), chant Gayatri Mantra 108 times, wear ruby or copper, donate wheat or jaggery on Sundays, perform Surya Namaskar.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Sun", "category", "upaya"));

        remedies.add("Moon Remedy: Offer white items like rice and milk, chant Om Chandraya Namaha 108 times, wear pearl or silver, donate white clothes, fast on Mondays, perform meditation during full moon.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Moon", "category", "upaya"));

        remedies.add("Mars Remedy: Chant Om Mangalaya Namaha, wear red coral, donate red lentils on Tuesdays, perform Hanuman Chalisa, avoid anger and aggression, offer coconut at temples.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Mars", "category", "upaya"));

        remedies.add("Mercury Remedy: Chant Om Budhaya Namaha, wear emerald or green, donate green items on Wednesdays, feed birds, practice communication discipline, study Vedas or scriptures.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Mercury", "category", "upaya"));

        remedies.add("Jupiter Remedy: Chant Om Gurave Namaha, wear yellow sapphire or topaz, donate yellow items on Thursdays, feed Brahmins or priests, perform Vishnu Puja, practice gratitude.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Jupiter", "category", "upaya"));

        remedies.add("Venus Remedy: Chant Om Shukraya Namaha, wear diamond or opal, donate white items on Fridays, perform Lakshmi Puja, offer fruits and sweets, practice art and creativity.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Venus", "category", "upaya"));

        remedies.add("Saturn Remedy: Chant Om Shanaishcharaya Namaha, wear blue sapphire or black, donate black items on Saturdays, feed the poor, serve the elderly, practice patience and discipline.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Saturn", "category", "upaya"));

        remedies.add("Rahu Remedy: Chant Om Rahave Namaha, wear gomedh (hessonite), donate blue or mixed items, perform Rahu Puja, avoid non-vegetarian food on Saturdays, practice honesty.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Rahu", "category", "upaya"));

        remedies.add("Ketu Remedy: Chant Om Ketave Namaha, wear cat's eye or chrysoberyl, donate mixed colors, perform Ketu Puja, practice spirituality and meditation, serve animals.");
        metadatas.add(Map.of("type", "planet_remedy", "planet", "Ketu", "category", "upaya"));

        remedies.add("General Protection: Chant Mahamrityunjaya Mantra for health and protection, perform Rudra Abhishekam, recite Hanuman Chalisa, practice yoga and meditation, observe Ekadashi fasts.");
        metadatas.add(Map.of("type", "general", "planet", "All", "category", "upaya"));

        chromaService.addDocuments("remedies", remedies, metadatas);
        log.info("Seeded {} remedies", remedies.size());
    }

    public void addUserMemory(String userId, String memory, String category) {
        List<Map<String, Object>> metadatas = List.of(Map.of(
                "user_id", userId,
                "category", category,
                "type", "user_memory",
                "timestamp", System.currentTimeMillis()
        ));
        chromaService.addDocuments("user_memory", List.of(memory), metadatas);
    }

    public List<String> searchVedicTexts(String query, int topK) {
        return chromaService.queryCollection("vedic_texts", query, topK);
    }

    public List<String> searchRemedies(String query, int topK) {
        return chromaService.queryCollection("remedies", query, topK);
    }

    public List<String> getUserMemories(String userId, int topK) {
        return chromaService.queryCollection("user_memory", userId + " memory", topK);
    }
}
