package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.service.AlbumService;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.dto.AlbumFilterOptionsDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Album}.
 */
@RestController
@RequestMapping("/api/albums")
public class AlbumResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlbumResource.class);

    private static final String ENTITY_NAME = "album";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlbumService albumService;

    private final AlbumRepository albumRepository;

    public AlbumResource(AlbumService albumService, AlbumRepository albumRepository) {
        this.albumService = albumService;
        this.albumRepository = albumRepository;
    }

    /**
     * {@code POST  /albums} : Create a new album.
     *
     * @param albumDTO the albumDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new albumDTO, or with status {@code 400 (Bad Request)} if the album has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AlbumDTO> createAlbum(@Valid @RequestBody AlbumDTO albumDTO) throws URISyntaxException {
        LOG.debug("REST request to save Album : {}", albumDTO);
        if (albumDTO.getId() != null) {
            throw new BadRequestAlertException("A new album cannot already have an ID", ENTITY_NAME, "idexists");
        }
        albumDTO = albumService.save(albumDTO);
        return ResponseEntity.created(new URI("/api/albums/" + albumDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, albumDTO.getId().toString()))
            .body(albumDTO);
    }

    /**
     * {@code PUT  /albums/:id} : Updates an existing album.
     *
     * @param id the id of the albumDTO to save.
     * @param albumDTO the albumDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated albumDTO,
     * or with status {@code 400 (Bad Request)} if the albumDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the albumDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<AlbumDTO> updateAlbum(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AlbumDTO albumDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Album : {}, {}", id, albumDTO);
        if (albumDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, albumDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!albumRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        albumDTO = albumService.update(albumDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, albumDTO.getId().toString()))
            .body(albumDTO);
    }

    /**
     * {@code PATCH  /albums/:id} : Partial updates given fields of an existing album, field will ignore if it is null
     *
     * @param id the id of the albumDTO to save.
     * @param albumDTO the albumDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated albumDTO,
     * or with status {@code 400 (Bad Request)} if the albumDTO is not valid,
     * or with status {@code 404 (Not Found)} if the albumDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the albumDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id:[0-9]+}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AlbumDTO> partialUpdateAlbum(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody AlbumDTO albumDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Album partially : {}, {}", id, albumDTO);
        if (albumDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, albumDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!albumRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AlbumDTO> result = albumService.partialUpdate(albumDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, albumDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /albums} : get all the albums.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of albums in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AlbumDTO>> getAllAlbums(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Albums");
        Page<AlbumDTO> page;
        if (eagerload) {
            page = albumService.findAllWithEagerRelationships(pageable);
        } else {
            page = albumService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /albums/gallery} : get all albums for gallery view with sorting.
     *
     * @param sortBy the sort criteria (EVENT or DATE).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of albums in body.
     */
    @GetMapping("/gallery")
    public ResponseEntity<List<AlbumDTO>> getGalleryAlbums(
        @RequestParam(name = "sortBy", required = false, defaultValue = "EVENT") String sortBy
    ) {
        LOG.debug("REST request to get Albums for gallery view with sort: {}", sortBy);
        List<AlbumDTO> albums = albumService.findAllForGallery(sortBy);
        return ResponseEntity.ok().body(albums);
    }

    /**
     * {@code GET  /albums/search} : search and filter albums.
     *
     * @param keyword the keyword to search in name, description, and keywords.
     * @param event the event name to filter by.
     * @param year the year to filter by.
     * @param tagName the tag name to filter by.
     * @param contributorLogin the contributor login to filter by.
     * @param sortBy the sort criteria (EVENT or DATE).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of filtered albums in body.
     */
    @GetMapping("/search")
    public ResponseEntity<List<AlbumDTO>> searchAndFilterAlbums(
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "event", required = false) String event,
        @RequestParam(name = "year", required = false) Integer year,
        @RequestParam(name = "tagName", required = false) String tagName,
        @RequestParam(name = "contributorLogin", required = false) String contributorLogin,
        @RequestParam(name = "sortBy", required = false, defaultValue = "EVENT") String sortBy
    ) {
        LOG.debug(
            "REST request to search and filter Albums with keyword: {}, event: {}, year: {}, tagName: {}, contributorLogin: {}, sortBy: {}",
            keyword,
            event,
            year,
            tagName,
            contributorLogin,
            sortBy
        );
        List<AlbumDTO> albums = albumService.searchAndFilter(keyword, event, year, tagName, contributorLogin, sortBy);
        return ResponseEntity.ok().body(albums);
    }

    /**
     * {@code GET  /albums/filter-options} : get available filter options.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and filter options in body.
     */
    @GetMapping("/filter-options")
    public ResponseEntity<AlbumFilterOptionsDTO> getFilterOptions() {
        LOG.debug("REST request to get Album filter options");
        AlbumFilterOptionsDTO filterOptions = albumService.getFilterOptions();
        return ResponseEntity.ok().body(filterOptions);
    }

    /**
     * {@code GET  /albums/:id} : get the "id" album.
     *
     * @param id the id of the albumDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the albumDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<AlbumDTO> getAlbum(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Album : {}", id);
        Optional<AlbumDTO> albumDTO = albumService.findOne(id);
        return ResponseUtil.wrapOrNotFound(albumDTO);
    }

    /**
     * {@code DELETE  /albums/:id} : delete the "id" album.
     *
     * @param id the id of the albumDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Album : {}", id);
        albumService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
