package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.AlbumSort;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.service.criteria.AlbumCriteria;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.mapper.AlbumMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Unit tests for {@link AlbumService}.
 * Tests album creation, sorting, validation, and business logic.
 */
@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private AlbumMapper albumMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
    private AlbumDTO albumDTO;
    private User user;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup test user
        user = new User();
        user.setId(1L);
        user.setLogin("testuser");

        // Setup test album
        album = new Album();
        album.setId(1L);
        album.setName("Test Album");
        album.setEvent("Test Event");
        album.setCreationDate(Instant.now());
        album.setUser(user);

        // Setup album DTO
        albumDTO = new AlbumDTO();
        albumDTO.setId(1L);
        albumDTO.setName("Test Album");
        albumDTO.setEvent("Test Event");
        albumDTO.setCreationDate(Instant.now());

        pageable = PageRequest.of(0, 20);
    }

    @Test
    void testSaveAlbum_Success() {
        // Given
        when(albumMapper.toEntity(albumDTO)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        AlbumDTO result = albumService.save(albumDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Album");
        verify(albumRepository).save(album);
    }

    @Test
    void testFindAlbumsSortedByEvent_Success() {
        // Given
        List<Album> albums = Arrays.asList(
            createAlbum("Album A", "Event Z"),
            createAlbum("Album B", "Event A"),
            createAlbum("Album C", null) // No event
        );
        Page<Album> albumPage = new PageImpl<>(albums);

        when(albumRepository.findAll(any(Pageable.class))).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        Pageable eventSortPageable = PageRequest.of(0, 20, Sort.by("event").ascending().and(Sort.by("name").ascending()));
        Page<AlbumDTO> result = albumService.findAll(eventSortPageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        verify(albumRepository).findAll(eventSortPageable);
    }

    @Test
    void testFindAlbumsSortedByDate_Success() {
        // Given
        List<Album> albums = Arrays.asList(
            createAlbumWithDate("Album Recent", Instant.now()),
            createAlbumWithDate("Album Old", Instant.now().minusSeconds(86400))
        );
        Page<Album> albumPage = new PageImpl<>(albums);

        when(albumRepository.findAll(any(Pageable.class))).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        Pageable dateSortPageable = PageRequest.of(0, 20, Sort.by("creationDate").descending());
        Page<AlbumDTO> result = albumService.findAll(dateSortPageable);

        // Then
        assertThat(result).isNotNull();
        verify(albumRepository).findAll(dateSortPageable);
    }

    @Test
    void testFindOne_Success() {
        // Given
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Optional<AlbumDTO> result = albumService.findOne(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void testFindOne_NotFound() {
        // Given
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<AlbumDTO> result = albumService.findOne(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testDelete_Success() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);

        // When
        albumService.delete(1L);

        // Then
        verify(albumRepository).deleteById(1L);
    }

    @Test
    void testValidateAlbumName_MinLength() {
        // Given
        album.setName("ab"); // Too short (< 3 chars)

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> albumService.validateAlbum(album));
    }

    @Test
    void testValidateAlbumName_MaxLength() {
        // Given
        String longName = "a".repeat(256); // Too long (> 255 chars)
        album.setName(longName);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> albumService.validateAlbum(album));
    }

    @Test
    void testValidateAlbumName_RequiredField() {
        // Given
        album.setName(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> albumService.validateAlbum(album));
    }

    @Test
    void testHandleMiscellaneousAlbums_Success() {
        // Given
        List<Album> albums = Arrays.asList(
            createAlbum("Album 1", null),
            createAlbum("Album 2", ""),
            createAlbum("Album 3", "   ") // Whitespace only
        );

        // When
        List<Album> result = albumService.categorizeMiscellaneousAlbums(albums);

        // Then
        assertThat(result).hasSize(3);
        result.forEach(a -> assertThat(a.getEvent()).isEqualTo("Miscellaneous"));
    }

    @Test
    void testPerformanceRequirement_ResponseTime() {
        // Given
        List<Album> largeAlbumList = createLargeAlbumList(1000);
        Page<Album> albumPage = new PageImpl<>(largeAlbumList);

        when(albumRepository.findAll(any(Pageable.class))).thenReturn(albumPage);
        when(albumMapper.toDto(any(Album.class))).thenReturn(albumDTO);

        // When
        long startTime = System.currentTimeMillis();
        Page<AlbumDTO> result = albumService.findAll(pageable);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Then
        assertThat(responseTime).isLessThan(2000); // < 2 seconds per requirement
        assertThat(result).isNotNull();
    }

    private Album createAlbum(String name, String event) {
        Album album = new Album();
        album.setName(name);
        album.setEvent(event);
        album.setCreationDate(Instant.now());
        album.setUser(user);
        return album;
    }

    private Album createAlbumWithDate(String name, Instant creationDate) {
        Album album = new Album();
        album.setName(name);
        album.setCreationDate(creationDate);
        album.setUser(user);
        return album;
    }

    private List<Album> createLargeAlbumList(int size) {
        return java.util.stream.IntStream.range(0, size)
            .mapToObj(i -> createAlbum("Album " + i, "Event " + (i % 10)))
            .collect(java.util.stream.Collectors.toList());
    }
}
