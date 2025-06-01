package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.dto.AlbumFilterOptionsDTO;
import com.mycompany.myapp.service.mapper.AlbumMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Album}.
 */
@Service
@Transactional
public class AlbumService {

    private static final Logger LOG = LoggerFactory.getLogger(AlbumService.class);

    private final AlbumRepository albumRepository;

    private final AlbumMapper albumMapper;

    public AlbumService(AlbumRepository albumRepository, AlbumMapper albumMapper) {
        this.albumRepository = albumRepository;
        this.albumMapper = albumMapper;
    }

    /**
     * Save a album.
     *
     * @param albumDTO the entity to save.
     * @return the persisted entity.
     */
    public AlbumDTO save(AlbumDTO albumDTO) {
        LOG.debug("Request to save Album : {}", albumDTO);
        Album album = albumMapper.toEntity(albumDTO);
        album = albumRepository.save(album);
        return albumMapper.toDto(album);
    }

    /**
     * Update a album.
     *
     * @param albumDTO the entity to save.
     * @return the persisted entity.
     */
    public AlbumDTO update(AlbumDTO albumDTO) {
        LOG.debug("Request to update Album : {}", albumDTO);
        Album album = albumMapper.toEntity(albumDTO);
        album = albumRepository.save(album);
        return albumMapper.toDto(album);
    }

    /**
     * Partially update a album.
     *
     * @param albumDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AlbumDTO> partialUpdate(AlbumDTO albumDTO) {
        LOG.debug("Request to partially update Album : {}", albumDTO);

        return albumRepository
            .findById(albumDTO.getId())
            .map(existingAlbum -> {
                albumMapper.partialUpdate(existingAlbum, albumDTO);

                return existingAlbum;
            })
            .map(albumRepository::save)
            .map(albumMapper::toDto);
    }

    /**
     * Get all the albums.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Albums");
        return albumRepository.findAll(pageable).map(albumMapper::toDto);
    }

    /**
     * Get all the albums with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AlbumDTO> findAllWithEagerRelationships(Pageable pageable) {
        return albumRepository.findAllWithEagerRelationships(pageable).map(albumMapper::toDto);
    }

    /**
     * Get all albums for gallery view with sorting.
     *
     * @param sortBy the sort criteria (EVENT or DATE).
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> findAllForGallery(String sortBy) {
        LOG.debug("Request to get all Albums for gallery with sort: {}", sortBy);
        List<Album> albums;
        if ("DATE".equalsIgnoreCase(sortBy)) {
            albums = albumRepository.findAllOrderByCreationDateDesc();
        } else {
            albums = albumRepository.findAllOrderByEventAsc();
        }
        return albums.stream().map(albumMapper::toDto).toList();
    }

    /**
     * Search and filter albums based on various criteria.
     *
     * @param keyword the keyword to search in name, description, and keywords.
     * @param event the event name to filter by.
     * @param year the year to filter by.
     * @param tagName the tag name to filter by.
     * @param contributorLogin the contributor login to filter by.
     * @param sortBy the sort criteria (EVENT or DATE).
     * @return the list of filtered entities.
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> searchAndFilter(
        String keyword,
        String event,
        Integer year,
        String tagName,
        String contributorLogin,
        String sortBy
    ) {
        LOG.debug(
            "Request to search and filter Albums with keyword: {}, event: {}, year: {}, tagName: {}, contributorLogin: {}, sortBy: {}",
            keyword,
            event,
            year,
            tagName,
            contributorLogin,
            sortBy
        );

        List<Album> albums = albumRepository.searchAndFilter(keyword, event, year, tagName, contributorLogin, sortBy);
        return albums.stream().map(albumMapper::toDto).toList();
    }

    /**
     * Get available filter options from existing albums.
     *
     * @return the filter options DTO.
     */
    @Transactional(readOnly = true)
    public AlbumFilterOptionsDTO getFilterOptions() {
        LOG.debug("Request to get Album filter options");

        List<String> events = albumRepository.findDistinctEvents();
        List<Integer> years = albumRepository.findDistinctYears();
        List<String> tags = albumRepository.findDistinctTagNames();
        List<String> contributors = albumRepository.findDistinctContributors();

        return new AlbumFilterOptionsDTO(events, years, tags, contributors);
    }

    /**
     * Get one album by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AlbumDTO> findOne(Long id) {
        LOG.debug("Request to get Album : {}", id);
        return albumRepository.findOneWithEagerRelationships(id).map(albumMapper::toDto);
    }

    /**
     * Delete the album by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Album : {}", id);
        albumRepository.deleteById(id);
    }
}
