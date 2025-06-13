package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Photo;
import com.mycompany.myapp.domain.PhotoTestSamples;
import com.mycompany.myapp.repository.PhotoRepository;
import com.mycompany.myapp.service.dto.PhotoDTO;
import com.mycompany.myapp.service.mapper.PhotoMapper;
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
 * Unit tests for {@link PhotoService}.
 */
@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private PhotoMapper photoMapper;

    private PhotoService photoService;

    private Photo photo;
    private PhotoDTO photoDTO;

    @BeforeEach
    void setUp() {
        photoService = new PhotoService(photoRepository, photoMapper);

        // Create test data
        photo = PhotoTestSamples.getPhotoSample1();
        photo.setUploadDate(Instant.now());

        photoDTO = new PhotoDTO();
        photoDTO.setId(1L);
        photoDTO.setTitle("Test Photo");
        photoDTO.setDescription("A beautiful test photo");
        photoDTO.setLocation("Test Location");
        photoDTO.setUploadDate(Instant.now());
        photoDTO.setKeywords("test, photo, beautiful");
    }

    @Test
    void shouldSavePhoto() {
        // Given
        when(photoMapper.toEntity(photoDTO)).thenReturn(photo);
        when(photoRepository.save(photo)).thenReturn(photo);
        when(photoMapper.toDto(photo)).thenReturn(photoDTO);

        // When
        PhotoDTO result = photoService.save(photoDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(photoDTO.getId());
        assertThat(result.getTitle()).isEqualTo(photoDTO.getTitle());
        assertThat(result.getDescription()).isEqualTo(photoDTO.getDescription());
        verify(photoRepository).save(photo);
        verify(photoMapper).toEntity(photoDTO);
        verify(photoMapper).toDto(photo);
    }

    @Test
    void shouldUpdatePhoto() {
        // Given
        when(photoMapper.toEntity(photoDTO)).thenReturn(photo);
        when(photoRepository.save(photo)).thenReturn(photo);
        when(photoMapper.toDto(photo)).thenReturn(photoDTO);

        // When
        PhotoDTO result = photoService.update(photoDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(photoDTO.getId());
        assertThat(result.getTitle()).isEqualTo(photoDTO.getTitle());
        verify(photoRepository).save(photo);
        verify(photoMapper).toEntity(photoDTO);
        verify(photoMapper).toDto(photo);
    }

    @Test
    void shouldPartiallyUpdatePhoto() {
        // Given
        Photo existingPhoto = PhotoTestSamples.getPhotoSample1();
        when(photoRepository.findById(photoDTO.getId())).thenReturn(Optional.of(existingPhoto));
        when(photoRepository.save(existingPhoto)).thenReturn(existingPhoto);
        when(photoMapper.toDto(existingPhoto)).thenReturn(photoDTO);
        doNothing().when(photoMapper).partialUpdate(existingPhoto, photoDTO);

        // When
        Optional<PhotoDTO> result = photoService.partialUpdate(photoDTO);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(photoDTO);
        verify(photoMapper).partialUpdate(existingPhoto, photoDTO);
        verify(photoRepository).save(existingPhoto);
    }

    @Test
    void shouldReturnEmptyWhenPartialUpdateWithNonExistentId() {
        // Given
        when(photoRepository.findById(photoDTO.getId())).thenReturn(Optional.empty());

        // When
        Optional<PhotoDTO> result = photoService.partialUpdate(photoDTO);

        // Then
        assertThat(result).isEmpty();
        verify(photoRepository).findById(photoDTO.getId());
    }

    @Test
    void shouldFindAllPhotos() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Photo> photoPage = new PageImpl<>(List.of(photo));
        when(photoRepository.findAll(pageable)).thenReturn(photoPage);
        when(photoMapper.toDto(photo)).thenReturn(photoDTO);

        // When
        Page<PhotoDTO> result = photoService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(photoDTO);
        verify(photoRepository).findAll(pageable);
    }

    @Test
    void shouldFindAllWithEagerRelationships() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Photo> photoPage = new PageImpl<>(List.of(photo));
        when(photoRepository.findAllWithEagerRelationships(pageable)).thenReturn(photoPage);
        when(photoMapper.toDto(photo)).thenReturn(photoDTO);

        // When
        Page<PhotoDTO> result = photoService.findAllWithEagerRelationships(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(photoDTO);
        verify(photoRepository).findAllWithEagerRelationships(pageable);
    }

    @Test
    void shouldFindOneById() {
        // Given
        Long id = 1L;
        when(photoRepository.findOneWithEagerRelationships(id)).thenReturn(Optional.of(photo));
        when(photoMapper.toDto(photo)).thenReturn(photoDTO);

        // When
        Optional<PhotoDTO> result = photoService.findOne(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(photoDTO);
        verify(photoRepository).findOneWithEagerRelationships(id);
    }

    @Test
    void shouldReturnEmptyWhenFindOneWithNonExistentId() {
        // Given
        Long id = 999L;
        when(photoRepository.findOneWithEagerRelationships(id)).thenReturn(Optional.empty());

        // When
        Optional<PhotoDTO> result = photoService.findOne(id);

        // Then
        assertThat(result).isEmpty();
        verify(photoRepository).findOneWithEagerRelationships(id);
    }

    @Test
    void shouldDeletePhoto() {
        // Given
        Long id = 1L;
        doNothing().when(photoRepository).deleteById(id);

        // When
        photoService.delete(id);

        // Then
        verify(photoRepository).deleteById(id);
    }

    @Test
    void shouldHandlePhotoWithNoTags() {
        // Given
        PhotoDTO photoDTOWithoutTags = new PhotoDTO();
        photoDTOWithoutTags.setId(2L);
        photoDTOWithoutTags.setTitle("Photo without tags");

        Photo photoWithoutTags = PhotoTestSamples.getPhotoSample2();

        when(photoMapper.toEntity(photoDTOWithoutTags)).thenReturn(photoWithoutTags);
        when(photoRepository.save(photoWithoutTags)).thenReturn(photoWithoutTags);
        when(photoMapper.toDto(photoWithoutTags)).thenReturn(photoDTOWithoutTags);

        // When
        PhotoDTO result = photoService.save(photoDTOWithoutTags);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(photoDTOWithoutTags.getId());
        verify(photoRepository).save(photoWithoutTags);
    }

    @Test
    void shouldHandlePhotoWithoutLocation() {
        // Given
        PhotoDTO photoDTOWithoutLocation = new PhotoDTO();
        photoDTOWithoutLocation.setId(3L);
        photoDTOWithoutLocation.setTitle("Photo without location");
        photoDTOWithoutLocation.setLocation(null);

        Photo photoWithoutLocation = PhotoTestSamples.getPhotoRandomSampleGenerator();
        photoWithoutLocation.setLocation(null);

        when(photoMapper.toEntity(photoDTOWithoutLocation)).thenReturn(photoWithoutLocation);
        when(photoRepository.save(photoWithoutLocation)).thenReturn(photoWithoutLocation);
        when(photoMapper.toDto(photoWithoutLocation)).thenReturn(photoDTOWithoutLocation);

        // When
        PhotoDTO result = photoService.save(photoDTOWithoutLocation);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(photoDTOWithoutLocation.getId());
        assertThat(result.getLocation()).isNull();
        verify(photoRepository).save(photoWithoutLocation);
    }

    @Test
    void shouldHandleEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Photo> emptyPage = new PageImpl<>(List.of());
        when(photoRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<PhotoDTO> result = photoService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(photoRepository).findAll(pageable);
    }
}
