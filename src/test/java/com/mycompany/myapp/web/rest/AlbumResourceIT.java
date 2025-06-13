package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.mapper.AlbumMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AlbumResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AlbumResourceIT {

    private static final String DEFAULT_NAME = "Test Album";
    private static final String UPDATED_NAME = "Updated Album";

    private static final String DEFAULT_EVENT = "Wedding";
    private static final String UPDATED_EVENT = "Birthday";

    private static final Instant DEFAULT_CREATION_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATION_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_OVERRIDE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_OVERRIDE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final byte[] DEFAULT_THUMBNAIL = new byte[] { 1, 2, 3 };
    private static final byte[] UPDATED_THUMBNAIL = new byte[] { 4, 5, 6 };
    private static final String DEFAULT_THUMBNAIL_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_THUMBNAIL_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_KEYWORDS = "vacation, beach, summer";
    private static final String UPDATED_KEYWORDS = "winter, snow, mountains";

    private static final String DEFAULT_DESCRIPTION = "A beautiful vacation album";
    private static final String UPDATED_DESCRIPTION = "A winter vacation album";

    private static final String ENTITY_API_URL = "/api/albums";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlbumMockMvc;

    private Album album;
    private Album insertedAlbum;

    /**
     * Create an entity for this test.
     */
    public static Album createEntity() {
        Album album = new Album();
        album.setName(DEFAULT_NAME);
        album.setEvent(DEFAULT_EVENT);
        album.setCreationDate(DEFAULT_CREATION_DATE);
        album.setOverrideDate(DEFAULT_OVERRIDE_DATE);
        album.setThumbnail(DEFAULT_THUMBNAIL);
        album.setThumbnailContentType(DEFAULT_THUMBNAIL_CONTENT_TYPE);
        album.setKeywords(DEFAULT_KEYWORDS);
        album.setDescription(DEFAULT_DESCRIPTION);
        return album;
    }

    /**
     * Create an updated entity for this test.
     */
    public static Album createUpdatedEntity() {
        Album album = new Album();
        album.setName(UPDATED_NAME);
        album.setEvent(UPDATED_EVENT);
        album.setCreationDate(UPDATED_CREATION_DATE);
        album.setOverrideDate(UPDATED_OVERRIDE_DATE);
        album.setThumbnail(UPDATED_THUMBNAIL);
        album.setThumbnailContentType(UPDATED_THUMBNAIL_CONTENT_TYPE);
        album.setKeywords(UPDATED_KEYWORDS);
        album.setDescription(UPDATED_DESCRIPTION);
        return album;
    }

    @BeforeEach
    void initTest() {
        album = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAlbum != null) {
            albumRepository.delete(insertedAlbum);
            insertedAlbum = null;
        }
    }

    @Test
    @Transactional
    void createAlbum() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create the Album
        AlbumDTO albumDTO = albumMapper.toDto(album);

        var returnedAlbumDTO = om.readValue(
            restAlbumMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlbumDTO.class
        );

        // Validate the Album in the database
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeCreate + 1);
        var returnedAlbum = albumMapper.toEntity(returnedAlbumDTO);
        insertedAlbum = returnedAlbum;
    }

    @Test
    @Transactional
    void createAlbumWithExistingId() throws Exception {
        // Create the Album with an existing ID
        album.setId(1L);
        AlbumDTO albumDTO = albumMapper.toDto(album);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAlbumMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Album in the database
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        album.setName(null);

        // Create the Album, which fails.
        AlbumDTO albumDTO = albumMapper.toDto(album);

        restAlbumMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
            .andExpect(status().isBadRequest());

        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreationDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        album.setCreationDate(null);

        // Create the Album, which fails.
        AlbumDTO albumDTO = albumMapper.toDto(album);

        restAlbumMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
            .andExpect(status().isBadRequest());

        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAlbums() throws Exception {
        // Initialize the database
        insertedAlbum = albumRepository.saveAndFlush(album);

        // Get all the albumList
        restAlbumMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(album.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].event").value(hasItem(DEFAULT_EVENT)))
            .andExpect(jsonPath("$.[*].creationDate").value(hasItem(DEFAULT_CREATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].keywords").value(hasItem(DEFAULT_KEYWORDS)));
    }

    @Test
    @Transactional
    void getAlbum() throws Exception {
        // Initialize the database
        insertedAlbum = albumRepository.saveAndFlush(album);

        // Get the album
        restAlbumMockMvc
            .perform(get(ENTITY_API_URL_ID, album.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(album.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.event").value(DEFAULT_EVENT))
            .andExpect(jsonPath("$.keywords").value(DEFAULT_KEYWORDS));
    }

    @Test
    @Transactional
    void getNonExistingAlbum() throws Exception {
        // Get the album
        restAlbumMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAlbum() throws Exception {
        // Initialize the database
        insertedAlbum = albumRepository.saveAndFlush(album);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the album
        Album updatedAlbum = albumRepository.findById(album.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAlbum are not directly saved in db
        em.detach(updatedAlbum);
        updatedAlbum.setName(UPDATED_NAME);
        updatedAlbum.setEvent(UPDATED_EVENT);
        updatedAlbum.setKeywords(UPDATED_KEYWORDS);
        updatedAlbum.setDescription(UPDATED_DESCRIPTION);

        AlbumDTO albumDTO = albumMapper.toDto(updatedAlbum);

        restAlbumMockMvc
            .perform(
                put(ENTITY_API_URL_ID, albumDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO))
            )
            .andExpect(status().isOk());

        // Validate the Album in the database
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAlbum() throws Exception {
        // Initialize the database
        insertedAlbum = albumRepository.saveAndFlush(album);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the album
        restAlbumMockMvc
            .perform(delete(ENTITY_API_URL_ID, album.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database is empty
        assertThat(getRepositoryCount()).isEqualTo(databaseSizeBeforeDelete - 1);
    }

    protected long getRepositoryCount() {
        return albumRepository.count();
    }
}
