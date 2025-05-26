package com.mycompany.myapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link AlbumRepository}.
 * Tests JDL requirements, sorting, pagination, and database operations.
 */
@DataJpaTest
@IntegrationTest
class AlbumRepositoryTest {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Album album1, album2, album3, album4;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setLogin("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setActivated(true);
        testUser = userRepository.saveAndFlush(testUser);

        // Create test albums for different sorting scenarios
        album1 = createAlbum("Wedding Photos", "Smith Wedding", Instant.now().minusSeconds(86400 * 3));
        album2 = createAlbum("Vacation Pics", "Europe Trip", Instant.now().minusSeconds(86400 * 2));
        album3 = createAlbum("Random Photos", null, Instant.now().minusSeconds(86400));
        album4 = createAlbum("Birthday Party", "John's Birthday", Instant.now());

        albumRepository.saveAll(List.of(album1, album2, album3, album4));
        entityManager.flush();
        entityManager.clear();
    }

    // User Story Requirement: Event-based sorting (alphabetically)
    @Test
    @Transactional
    void testFindAllSortedByEventName_Ascending() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("event").ascending().and(Sort.by("name").ascending()));

        // When
        Page<Album> result = albumRepository.findAll(pageable);

        // Then
        assertThat(result.getContent()).hasSize(4);

        // Verify sorting: nulls first, then alphabetical by event
        List<Album> albums = result.getContent();
        assertThat(albums.get(0).getEvent()).isNull(); // Miscellaneous first
        assertThat(albums.get(1).getEvent()).isEqualTo("Europe Trip");
        assertThat(albums.get(2).getEvent()).isEqualTo("John's Birthday");
        assertThat(albums.get(3).getEvent()).isEqualTo("Smith Wedding");
    }

    // User Story Requirement: Date-based sorting (chronological, recent first)
    @Test
    @Transactional
    void testFindAllSortedByCreationDate_Descending() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("creationDate").descending());

        // When
        Page<Album> result = albumRepository.findAll(pageable);

        // Then
        assertThat(result.getContent()).hasSize(4);

        // Verify sorting: most recent first
        List<Album> albums = result.getContent();
        assertThat(albums.get(0).getName()).isEqualTo("Birthday Party");
        assertThat(albums.get(1).getName()).isEqualTo("Random Photos");
        assertThat(albums.get(2).getName()).isEqualTo("Vacation Pics");
        assertThat(albums.get(3).getName()).isEqualTo("Wedding Photos");
    }

    // JDL Requirement: ManyToOne relationship with User
    @Test
    @Transactional
    void testFindByUserLogin() {
        // When
        List<Album> result = albumRepository.findByUserLogin("testuser");

        // Then
        assertThat(result).hasSize(4);
        result.forEach(album -> assertThat(album.getUser().getLogin()).isEqualTo("testuser"));
    }

    // User Story Requirement: Handle albums without events (Miscellaneous)
    @Test
    @Transactional
    void testFindByEventIsNull() {
        // When
        List<Album> result = albumRepository.findByEventIsNull();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Random Photos");
    }

    @Test
    @Transactional
    void testFindByEventIsNotNull() {
        // When
        List<Album> result = albumRepository.findByEventIsNotNull();

        // Then
        assertThat(result).hasSize(3);
        result.forEach(album -> assertThat(album.getEvent()).isNotNull());
    }

    // JDL Requirement: Pagination
    @Test
    @Transactional
    void testPaginationWorksCorrectly() {
        // Given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);

        // When
        Page<Album> page1 = albumRepository.findAll(firstPage);
        Page<Album> page2 = albumRepository.findAll(secondPage);

        // Then
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getTotalPages()).isEqualTo(2);

        // Ensure no overlap between pages
        List<Long> page1Ids = page1.getContent().stream().map(Album::getId).toList();
        List<Long> page2Ids = page2.getContent().stream().map(Album::getId).toList();

        assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);
    }

    // User Story Requirement: Thumbnail handling
    @Test
    @Transactional
    void testThumbnailHandling() {
        // Given
        album1.setThumbnail("thumbnail1".getBytes());
        album1.setThumbnailContentType("image/jpeg");
        album2.setThumbnail(null);
        albumRepository.saveAndFlush(album1);
        albumRepository.saveAndFlush(album2);

        // When
        List<Album> albumsWithThumbnails = albumRepository.findByThumbnailIsNotNull();

        // Then
        assertThat(albumsWithThumbnails).hasSize(1);
        assertThat(albumsWithThumbnails.get(0).getId()).isEqualTo(album1.getId());
        assertThat(albumsWithThumbnails.get(0).getThumbnailContentType()).isEqualTo("image/jpeg");
    }

    // User Story Requirement: Date override functionality
    @Test
    @Transactional
    void testOverrideDateHandling() {
        // Given
        Instant overrideDate = Instant.now().minusSeconds(7200);
        album1.setOverrideDate(overrideDate);
        albumRepository.saveAndFlush(album1);

        // When
        Album retrieved = albumRepository.findById(album1.getId()).orElseThrow();

        // Then
        assertThat(retrieved.getOverrideDate()).isEqualTo(overrideDate);
        assertThat(retrieved.getCreationDate()).isNotEqualTo(overrideDate);
    }

    // Non-Functional Requirement: Performance with large datasets
    @Test
    @Transactional
    void testPerformanceWithLargeDataset() {
        // Given - Create 50 additional albums
        for (int i = 0; i < 50; i++) {
            Album album = createAlbum("Album " + i, "Event " + (i % 10), Instant.now().minusSeconds(i));
            albumRepository.save(album);
        }
        entityManager.flush();

        // When
        long startTime = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, 20, Sort.by("creationDate").descending());
        Page<Album> result = albumRepository.findAll(pageable);
        long endTime = System.currentTimeMillis();

        // Then
        assertThat(endTime - startTime).isLessThan(1000); // < 1 second
        assertThat(result.getContent()).hasSize(20);
        assertThat(result.getTotalElements()).isEqualTo(54); // 4 + 50
    }

    // JDL Requirement: Name validation constraints
    @Test
    @Transactional
    void testNameValidationConstraints() {
        // Test valid name (3-255 characters)
        Album validAlbum = createAlbum("Valid Album Name", "Event", Instant.now());
        Album saved = albumRepository.saveAndFlush(validAlbum);
        assertThat(saved.getName()).isEqualTo("Valid Album Name");

        // Test minimum length requirement
        assertThat(saved.getName().length()).isGreaterThanOrEqualTo(3);

        // Test maximum length requirement
        assertThat(saved.getName().length()).isLessThanOrEqualTo(255);
    }

    // JDL Requirement: Event max length validation
    @Test
    @Transactional
    void testEventValidationConstraints() {
        // Test valid event (max 255 characters)
        String validEvent = "Valid Event Name";
        Album validAlbum = createAlbum("Album", validEvent, Instant.now());
        Album saved = albumRepository.saveAndFlush(validAlbum);

        assertThat(saved.getEvent()).isEqualTo(validEvent);
        assertThat(saved.getEvent().length()).isLessThanOrEqualTo(255);
    }

    // User Story Requirement: Eager loading relationships
    @Test
    @Transactional
    void testEagerLoadingRelationships() {
        // When
        Page<Album> albums = albumRepository.findAllWithEagerRelationships(PageRequest.of(0, 10));

        // Then
        assertThat(albums.getContent()).isNotEmpty();
        albums
            .getContent()
            .forEach(album -> {
                assertThat(album.getUser()).isNotNull();
                assertThat(album.getUser().getLogin()).isEqualTo("testuser");
            });
    }

    private Album createAlbum(String name, String event, Instant creationDate) {
        Album album = new Album();
        album.setName(name);
        album.setEvent(event);
        album.setCreationDate(creationDate);
        album.setUser(testUser);
        return album;
    }
}
