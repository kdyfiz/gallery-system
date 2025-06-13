package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.AlbumTestSamples;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.mapper.AlbumMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link AlbumService}.
 */
@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private AlbumMapper albumMapper;

    private AlbumService albumService;

    private Album album;
    private AlbumDTO albumDTO;

    @BeforeEach
    void setUp() {
        albumService = new AlbumService(albumRepository, albumMapper);

        // Create test data
        album = AlbumTestSamples.getAlbumSample1();
        album.setCreationDate(Instant.now());

        albumDTO = new AlbumDTO();
        albumDTO.setId(1L);
        albumDTO.setName("Test Album");
        albumDTO.setEvent("Test Event");
        albumDTO.setCreationDate(Instant.now());
        albumDTO.setKeywords("test, album, photo");
    }

    @Test
    void shouldSaveAlbum() {
        // Given
        when(albumMapper.toEntity(albumDTO)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        AlbumDTO result = albumService.save(albumDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(albumDTO.getId());
        assertThat(result.getName()).isEqualTo(albumDTO.getName());
        verify(albumRepository).save(album);
        verify(albumMapper).toEntity(albumDTO);
        verify(albumMapper).toDto(album);
    }

    @Test
    void shouldUpdateAlbum() {
        // Given
        when(albumMapper.toEntity(albumDTO)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        AlbumDTO result = albumService.update(albumDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(albumDTO.getId());
        verify(albumRepository).save(album);
    }

    @Test
    void shouldPartiallyUpdateAlbum() {
        // Given
        Album existingAlbum = AlbumTestSamples.getAlbumSample1();
        when(albumRepository.findById(albumDTO.getId())).thenReturn(Optional.of(existingAlbum));
        when(albumRepository.save(existingAlbum)).thenReturn(existingAlbum);
        when(albumMapper.toDto(existingAlbum)).thenReturn(albumDTO);
        doNothing().when(albumMapper).partialUpdate(existingAlbum, albumDTO);

        // When
        Optional<AlbumDTO> result = albumService.partialUpdate(albumDTO);

        // Then
        assertThat(result).isPresent();
        verify(albumMapper).partialUpdate(existingAlbum, albumDTO);
        verify(albumRepository).save(existingAlbum);
    }

    @Test
    void shouldReturnEmptyWhenPartialUpdateWithNonExistentId() {
        // Given
        when(albumRepository.findById(albumDTO.getId())).thenReturn(Optional.empty());

        // When
        Optional<AlbumDTO> result = albumService.partialUpdate(albumDTO);

        // Then
        assertThat(result).isEmpty();
        verify(albumRepository).findById(albumDTO.getId());
    }

    @Test
    void shouldFindAllAlbums() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findAll(pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(albumDTO);
        verify(albumRepository).findAll(pageable);
    }

    @Test
    void shouldFindAllWithEagerRelationships() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findAllWithEagerRelationships(pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAllWithEagerRelationships(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findAllWithEagerRelationships(pageable);
    }

    @Test
    void shouldFindAllOrderByEvent() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findAllOrderByEventThenName(pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAllOrderByEvent(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findAllOrderByEventThenName(pageable);
    }

    @Test
    void shouldFindAllOrderByDate() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findAllOrderByDateDesc(pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findAllOrderByDate(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findAllOrderByDateDesc(pageable);
    }

    @Test
    void shouldSearchByKeyword() {
        // Given
        String keyword = "vacation";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findByKeyword(keyword, pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.searchByKeyword(keyword, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findByKeyword(keyword, pageable);
    }

    @Test
    void shouldFindByEvent() {
        // Given
        String event = "Wedding";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findByEvent(event, pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findByEvent(event, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findByEvent(event, pageable);
    }

    @Test
    void shouldFindByYear() {
        // Given
        Integer year = 2023;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findByYear(year, pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findByYear(year, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findByYear(year, pageable);
    }

    @Test
    void shouldFindByTagName() {
        // Given
        String tagName = "nature";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findByTagName(tagName, pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findByTagName(tagName, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findByTagName(tagName, pageable);
    }

    @Test
    void shouldFindByContributor() {
        // Given
        String contributorLogin = "john.doe";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));
        when(albumRepository.findByContributor(contributorLogin, pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.findByContributor(contributorLogin, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findByContributor(contributorLogin, pageable);
    }

    @Test
    void shouldSearchAndFilterWithMultipleCriteria() {
        // Given
        String keyword = "vacation";
        String event = "Summer";
        Integer year = 2023;
        String tagName = "beach";
        String contributorLogin = "john.doe";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));

        when(albumRepository.findBySearchAndFilters(keyword, event, year, tagName, contributorLogin, pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.searchAndFilter(keyword, event, year, tagName, contributorLogin, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findBySearchAndFilters(keyword, event, year, tagName, contributorLogin, pageable);
    }

    @Test
    void shouldFindOneById() {
        // Given
        Long id = 1L;
        when(albumRepository.findOneWithEagerRelationships(id)).thenReturn(Optional.of(album));
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Optional<AlbumDTO> result = albumService.findOne(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(albumDTO);
        verify(albumRepository).findOneWithEagerRelationships(id);
    }

    @Test
    void shouldReturnEmptyWhenFindOneWithNonExistentId() {
        // Given
        Long id = 999L;
        when(albumRepository.findOneWithEagerRelationships(id)).thenReturn(Optional.empty());

        // When
        Optional<AlbumDTO> result = albumService.findOne(id);

        // Then
        assertThat(result).isEmpty();
        verify(albumRepository).findOneWithEagerRelationships(id);
    }

    @Test
    void shouldDeleteAlbum() {
        // Given
        Long id = 1L;
        doNothing().when(albumRepository).deleteById(id);

        // When
        albumService.delete(id);

        // Then
        verify(albumRepository).deleteById(id);
    }

    @Test
    void shouldHandleNullParametersInSearchAndFilter() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Album> albumPage = new PageImpl<>(List.of(album));

        when(albumRepository.findBySearchAndFilters(null, null, null, null, null, pageable)).thenReturn(albumPage);
        when(albumMapper.toDto(album)).thenReturn(albumDTO);

        // When
        Page<AlbumDTO> result = albumService.searchAndFilter(null, null, null, null, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(albumRepository).findBySearchAndFilters(null, null, null, null, null, pageable);
    }
}
