package com.mycompany.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycompany.myapp.domain.Tag;
import com.mycompany.myapp.domain.TagTestSamples;
import com.mycompany.myapp.repository.TagRepository;
import com.mycompany.myapp.service.dto.TagDTO;
import com.mycompany.myapp.service.mapper.TagMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link TagService}.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    private TagService tagService;

    private Tag tag;
    private TagDTO tagDTO;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository, tagMapper);

        // Create test data
        tag = TagTestSamples.getTagSample1();

        tagDTO = new TagDTO();
        tagDTO.setId(1L);
        tagDTO.setName("nature");
    }

    @Test
    void shouldSaveTag() {
        // Given
        when(tagMapper.toEntity(tagDTO)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        TagDTO result = tagService.save(tagDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tagDTO.getId());
        assertThat(result.getName()).isEqualTo(tagDTO.getName());
        verify(tagRepository).save(tag);
        verify(tagMapper).toEntity(tagDTO);
        verify(tagMapper).toDto(tag);
    }

    @Test
    void shouldUpdateTag() {
        // Given
        when(tagMapper.toEntity(tagDTO)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        TagDTO result = tagService.update(tagDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(tagDTO.getId());
        assertThat(result.getName()).isEqualTo(tagDTO.getName());
        verify(tagRepository).save(tag);
        verify(tagMapper).toEntity(tagDTO);
        verify(tagMapper).toDto(tag);
    }

    @Test
    void shouldPartiallyUpdateTag() {
        // Given
        Tag existingTag = TagTestSamples.getTagSample1();
        when(tagRepository.findById(tagDTO.getId())).thenReturn(Optional.of(existingTag));
        when(tagRepository.save(existingTag)).thenReturn(existingTag);
        when(tagMapper.toDto(existingTag)).thenReturn(tagDTO);
        doNothing().when(tagMapper).partialUpdate(existingTag, tagDTO);

        // When
        Optional<TagDTO> result = tagService.partialUpdate(tagDTO);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(tagDTO);
        verify(tagMapper).partialUpdate(existingTag, tagDTO);
        verify(tagRepository).save(existingTag);
    }

    @Test
    void shouldReturnEmptyWhenPartialUpdateWithNonExistentId() {
        // Given
        when(tagRepository.findById(tagDTO.getId())).thenReturn(Optional.empty());

        // When
        Optional<TagDTO> result = tagService.partialUpdate(tagDTO);

        // Then
        assertThat(result).isEmpty();
        verify(tagRepository).findById(tagDTO.getId());
    }

    @Test
    void shouldFindAllTags() {
        // Given
        Tag tag2 = TagTestSamples.getTagSample2();
        TagDTO tagDTO2 = new TagDTO();
        tagDTO2.setId(2L);
        tagDTO2.setName("landscape");

        List<Tag> tags = List.of(tag, tag2);
        when(tagRepository.findAll()).thenReturn(tags);
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);
        when(tagMapper.toDto(tag2)).thenReturn(tagDTO2);

        // When
        List<TagDTO> result = tagService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(tagDTO);
        assertThat(result.get(1)).isEqualTo(tagDTO2);
        verify(tagRepository).findAll();
    }

    @Test
    void shouldFindAllTagsWhenEmpty() {
        // Given
        when(tagRepository.findAll()).thenReturn(List.of());

        // When
        List<TagDTO> result = tagService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(tagRepository).findAll();
    }

    @Test
    void shouldFindOneById() {
        // Given
        Long id = 1L;
        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));
        when(tagMapper.toDto(tag)).thenReturn(tagDTO);

        // When
        Optional<TagDTO> result = tagService.findOne(id);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(tagDTO);
        verify(tagRepository).findById(id);
    }

    @Test
    void shouldReturnEmptyWhenFindOneWithNonExistentId() {
        // Given
        Long id = 999L;
        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<TagDTO> result = tagService.findOne(id);

        // Then
        assertThat(result).isEmpty();
        verify(tagRepository).findById(id);
    }

    @Test
    void shouldDeleteTag() {
        // Given
        Long id = 1L;
        doNothing().when(tagRepository).deleteById(id);

        // When
        tagService.delete(id);

        // Then
        verify(tagRepository).deleteById(id);
    }

    @Test
    void shouldHandleTagWithSpecialCharacters() {
        // Given
        TagDTO specialTagDTO = new TagDTO();
        specialTagDTO.setId(3L);
        specialTagDTO.setName("café&nature");

        Tag specialTag = TagTestSamples.getTagRandomSampleGenerator();
        specialTag.setName("café&nature");

        when(tagMapper.toEntity(specialTagDTO)).thenReturn(specialTag);
        when(tagRepository.save(specialTag)).thenReturn(specialTag);
        when(tagMapper.toDto(specialTag)).thenReturn(specialTagDTO);

        // When
        TagDTO result = tagService.save(specialTagDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("café&nature");
        verify(tagRepository).save(specialTag);
    }

    @Test
    void shouldHandleTagWithLongName() {
        // Given
        String longName = "a".repeat(100); // Assuming tags can have long names
        TagDTO longTagDTO = new TagDTO();
        longTagDTO.setId(4L);
        longTagDTO.setName(longName);

        Tag longTag = TagTestSamples.getTagRandomSampleGenerator();
        longTag.setName(longName);

        when(tagMapper.toEntity(longTagDTO)).thenReturn(longTag);
        when(tagRepository.save(longTag)).thenReturn(longTag);
        when(tagMapper.toDto(longTag)).thenReturn(longTagDTO);

        // When
        TagDTO result = tagService.save(longTagDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).hasSize(100);
        verify(tagRepository).save(longTag);
    }

    @Test
    void shouldHandleTagWithCaseVariations() {
        // Given
        TagDTO upperCaseTagDTO = new TagDTO();
        upperCaseTagDTO.setId(5L);
        upperCaseTagDTO.setName("NATURE");

        Tag upperCaseTag = TagTestSamples.getTagRandomSampleGenerator();
        upperCaseTag.setName("NATURE");

        when(tagMapper.toEntity(upperCaseTagDTO)).thenReturn(upperCaseTag);
        when(tagRepository.save(upperCaseTag)).thenReturn(upperCaseTag);
        when(tagMapper.toDto(upperCaseTag)).thenReturn(upperCaseTagDTO);

        // When
        TagDTO result = tagService.save(upperCaseTagDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("NATURE");
        verify(tagRepository).save(upperCaseTag);
    }

    @Test
    void shouldHandleMultipleTagsWithSameName() {
        // Given - Testing uniqueness constraint behavior
        TagDTO duplicateTagDTO = new TagDTO();
        duplicateTagDTO.setId(6L);
        duplicateTagDTO.setName("nature"); // Same name as original tag

        Tag duplicateTag = TagTestSamples.getTagRandomSampleGenerator();
        duplicateTag.setName("nature");

        when(tagMapper.toEntity(duplicateTagDTO)).thenReturn(duplicateTag);
        when(tagRepository.save(duplicateTag)).thenReturn(duplicateTag);
        when(tagMapper.toDto(duplicateTag)).thenReturn(duplicateTagDTO);

        // When
        TagDTO result = tagService.save(duplicateTagDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("nature");
        verify(tagRepository).save(duplicateTag);
    }
}
