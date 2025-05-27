package com.mycompany.myapp.config;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.repository.AlbumRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("testdev")
public class TestDataGenerator {

    @Autowired
    private AlbumRepository albumRepository;

    private final Random random = new Random();
    private final List<String> events = List.of(
        "Summer Vacation",
        "Birthday Party",
        "Wedding",
        "Family Reunion",
        "Holiday Trip",
        "Company Event",
        "Graduation",
        "Anniversary",
        "Concert",
        "Sports Event"
    );

    @EventListener(ApplicationReadyEvent.class)
    public void generateTestData() {
        // Generate 1000 test albums
        for (int i = 0; i < 1000; i++) {
            Album album = new Album();
            album.setName("Test Album " + i);

            // 80% of albums have events, 20% don't (for testing Miscellaneous grouping)
            if (random.nextFloat() < 0.8) {
                album.setEvent(events.get(random.nextInt(events.size())));
            }

            // Random date within last 2 years
            long daysToSubtract = random.nextInt(365 * 2);
            album.setCreationDate(Instant.now().minus(daysToSubtract, ChronoUnit.DAYS));

            // 30% chance of having an override date
            if (random.nextFloat() < 0.3) {
                album.setOverrideDate(album.getCreationDate().plus(random.nextInt(30), ChronoUnit.DAYS));
            }

            albumRepository.save(album);
        }
    }
}
