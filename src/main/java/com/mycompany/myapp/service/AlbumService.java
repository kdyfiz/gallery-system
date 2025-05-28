package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.mapper.AlbumMapper;
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
     * Get albums sorted by event name (with Miscellaneous category for albums without events).
     *
     * @param pageable the pagination information.
     * @return the list of entities sorted by event.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> findAllOrderByEvent(Pageable pageable) {
        LOG.debug("Request to get all Albums ordered by event");
        return albumRepository.findAllOrderByEventThenName(pageable).map(albumMapper::toDto);
    }

    /**
     * Get albums sorted by date (most recent first, using override date if available).
     *
     * @param pageable the pagination information.
     * @return the list of entities sorted by date.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> findAllOrderByDate(Pageable pageable) {
        LOG.debug("Request to get all Albums ordered by date");
        return albumRepository.findAllOrderByDateDesc(pageable).map(albumMapper::toDto);
    }

    /**
     * Search albums by keyword (searches in name, keywords, and description fields).
     *
     * @param keyword the search keyword.
     * @param pageable the pagination information.
     * @return the list of matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> searchByKeyword(String keyword, Pageable pageable) {
        LOG.debug("Request to search Albums by keyword : {}", keyword);
        return albumRepository.findByKeyword(keyword, pageable).map(albumMapper::toDto);
    }

    /**
     * Filter albums by event name.
     *
     * @param event the event name to filter by.
     * @param pageable the pagination information.
     * @return the list of matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByEvent(String event, Pageable pageable) {
        LOG.debug("Request to find Albums by event : {}", event);
        return albumRepository.findByEvent(event, pageable).map(albumMapper::toDto);
    }

    /**
     * Filter albums by year.
     *
     * @param year the year to filter by.
     * @param pageable the pagination information.
     * @return the list of matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByYear(Integer year, Pageable pageable) {
        LOG.debug("Request to find Albums by year : {}", year);
        return albumRepository.findByYear(year, pageable).map(albumMapper::toDto);
    }

    /**
     * Filter albums by tag name.
     *
     * @param tagName the tag name to filter by.
     * @param pageable the pagination information.
     * @return the list of matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByTagName(String tagName, Pageable pageable) {
        LOG.debug("Request to find Albums by tag name : {}", tagName);
        return albumRepository.findByTagName(tagName, pageable).map(albumMapper::toDto);
    }

    /**
     * Filter albums by contributor (user login).
     *
     * @param contributorLogin the contributor login to filter by.
     * @param pageable the pagination information.
     * @return the list of matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByContributor(String contributorLogin, Pageable pageable) {
        LOG.debug("Request to find Albums by contributor : {}", contributorLogin);
        return albumRepository.findByContributor(contributorLogin, pageable).map(albumMapper::toDto);
    }

    /**
     * Search and filter albums using multiple criteria.
     *
     * @param keyword the search keyword (optional).
     * @param event the event name filter (optional).
     * @param year the year filter (optional).
     * @param tagName the tag name filter (optional).
     * @param contributorLogin the contributor filter (optional).
     * @param pageable the pagination information.
     * @return the list of matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlbumDTO> searchAndFilter(
        String keyword,
        String event,
        Integer year,
        String tagName,
        String contributorLogin,
        Pageable pageable
    ) {
        LOG.debug(
            "Request to search and filter Albums with keyword: {}, event: {}, year: {}, tag: {}, contributor: {}",
            keyword,
            event,
            year,
            tagName,
            contributorLogin
        );
        return albumRepository.findBySearchAndFilters(keyword, event, year, tagName, contributorLogin, pageable).map(albumMapper::toDto);
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
