package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.AlbumAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.AlbumRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.AlbumService;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.mapper.AlbumMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AlbumResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AlbumResourceIT {

    private static final String DEFAULT_NAME = "Test Album";
    private static final String UPDATED_NAME = "Updated Album";
    private static final String INVALID_SHORT_NAME = "AB"; // Less than 3 chars
    private static final String INVALID_LONG_NAME = "A".repeat(256); // More than 255 chars

    private static final String DEFAULT_EVENT = "Wedding";
    private static final String UPDATED_EVENT = "Birthday Party";

    private static final Instant DEFAULT_CREATION_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATION_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_OVERRIDE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_OVERRIDE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final byte[] DEFAULT_THUMBNAIL = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_THUMBNAIL = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_THUMBNAIL_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_THUMBNAIL_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/albums";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_GALLERY_API_URL = "/api/albums/gallery";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private AlbumRepository albumRepositoryMock;

    @Autowired
    private AlbumMapper albumMapper;

    @Mock
    private AlbumService albumServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAlbumMockMvc;

    private Album album;
    private Album insertedAlbum;
    private User testUser;

    /**
     * Create an entity for this test.
     */
    public static Album createEntity() {
        return new Album()
            .name(DEFAULT_NAME)
            .event(DEFAULT_EVENT)
            .creationDate(DEFAULT_CREATION_DATE)
            .overrideDate(DEFAULT_OVERRIDE_DATE)
            .thumbnail(DEFAULT_THUMBNAIL)
            .thumbnailContentType(DEFAULT_THUMBNAIL_CONTENT_TYPE);
    }

    /**
     * Create an updated entity for this test.
     */
    public static Album createUpdatedEntity() {
        return new Album()
            .name(UPDATED_NAME)
            .event(UPDATED_EVENT)
            .creationDate(UPDATED_CREATION_DATE)
            .overrideDate(UPDATED_OVERRIDE_DATE)
            .thumbnail(UPDATED_THUMBNAIL)
            .thumbnailContentType(UPDATED_THUMBNAIL_CONTENT_TYPE);
    }

    @BeforeEach
    void initTest() {
        album = createEntity();
        // Create test user
        testUser = new User();
        testUser.setLogin("testuser");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setActivated(true);
        testUser = userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    void cleanup() {
        if (insertedAlbum != null) {
            albumRepository.delete(insertedAlbum);
            insertedAlbum = null;
        }
        if (testUser != null && testUser.getId() != null) {
            userRepository.delete(testUser);
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
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlbum = albumMapper.toEntity(returnedAlbumDTO);
        assertAlbumUpdatableFieldsEquals(returnedAlbum, getPersistedAlbum(returnedAlbum));

        insertedAlbum = returnedAlbum;
    }

    @Test
    @Transactional
    void createAlbumWithMinimalData() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();

        // Create album with only required fields
        Album minimalAlbum = new Album().name("Minimal Album").creationDate(Instant.now());

        AlbumDTO albumDTO = albumMapper.toDto(minimalAlbum);

        var returnedAlbumDTO = om.readValue(
            restAlbumMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AlbumDTO.class
        );

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAlbum = albumMapper.toEntity(returnedAlbumDTO);
        assertThat(returnedAlbum.getName()).isEqualTo("Minimal Album");
        assertThat(returnedAlbum.getEvent()).isNull();
        assertThat(returnedAlbum.getThumbnail()).isNull();

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
        assertSameRepositoryCount(databaseSizeBeforeCreate);
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

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameMinLengthValidation() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();

        album.setName(INVALID_SHORT_NAME);
        AlbumDTO albumDTO = albumMapper.toDto(album);

        restAlbumMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameMaxLengthValidation() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();

        album.setName(INVALID_LONG_NAME);
        AlbumDTO albumDTO = albumMapper.toDto(album);

        restAlbumMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
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

        assertSameRepositoryCount(databaseSizeBeforeTest);
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
            .andExpect(jsonPath("$.[*].overrideDate").value(hasItem(DEFAULT_OVERRIDE_DATE.toString())))
            .andExpect(jsonPath("$.[*].thumbnailContentType").value(hasItem(DEFAULT_THUMBNAIL_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].thumbnail").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_THUMBNAIL))));
    }

    @Test
    @Transactional
    void getAlbumsForGalleryByEvent() throws Exception {
        // Create test albums with different events
        Album album1 = new Album().name("Wedding Album 1").event("Wedding").creationDate(Instant.now());
        Album album2 = new Album().name("Wedding Album 2").event("Wedding").creationDate(Instant.now());
        Album album3 = new Album().name("Birthday Album").event("Birthday").creationDate(Instant.now());
        Album album4 = new Album().name("Random Album").creationDate(Instant.now()); // No event

        albumRepository.saveAndFlush(album1);
        albumRepository.saveAndFlush(album2);
        albumRepository.saveAndFlush(album3);
        albumRepository.saveAndFlush(album4);

        // Get gallery view sorted by event
        restAlbumMockMvc
            .perform(get(ENTITY_GALLERY_API_URL + "?sortBy=EVENT"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(4))); // Should return all albums
    }

    @Test
    @Transactional
    void getAlbumsForGalleryByDate() throws Exception {
        Instant now = Instant.now();

        // Create test albums with different dates
        Album album1 = new Album().name("Oldest Album").creationDate(now.minus(30, ChronoUnit.DAYS));
        Album album2 = new Album().name("Recent Album").creationDate(now.minus(1, ChronoUnit.DAYS));
        Album album3 = new Album().name("Override Date Album").creationDate(now.minus(10, ChronoUnit.DAYS)).overrideDate(now); // Should be sorted by override date

        albumRepository.saveAndFlush(album1);
        albumRepository.saveAndFlush(album2);
        albumRepository.saveAndFlush(album3);

        // Get gallery view sorted by date
        restAlbumMockMvc
            .perform(get(ENTITY_GALLERY_API_URL + "?sortBy=DATE"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @Transactional
    @WithMockUser(username = "user1")
    void getUserSpecificAlbums() throws Exception {
        // Create user-specific albums
        User user1 = new User();
        user1.setLogin("user1");
        user1 = userRepository.saveAndFlush(user1);

        User user2 = new User();
        user2.setLogin("user2");
        user2 = userRepository.saveAndFlush(user2);

        Album album1 = new Album().name("User1 Album").creationDate(Instant.now()).user(user1);
        Album album2 = new Album().name("User2 Album").creationDate(Instant.now()).user(user2);

        albumRepository.saveAndFlush(album1);
        albumRepository.saveAndFlush(album2);

        // Should only return user1's albums
        restAlbumMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$[*].name").value(hasItem("User1 Album")))
            .andExpect(jsonPath("$[*].name").value(not(hasItem("User2 Album"))));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlbumsWithEagerRelationshipsIsEnabled() throws Exception {
        when(albumServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlbumMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(albumServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllAlbumsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(albumServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restAlbumMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(albumRepositoryMock, times(1)).findAll(any(Pageable.class));
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
            .andExpect(jsonPath("$.creationDate").value(DEFAULT_CREATION_DATE.toString()))
            .andExpect(jsonPath("$.overrideDate").value(DEFAULT_OVERRIDE_DATE.toString()))
            .andExpect(jsonPath("$.thumbnailContentType").value(DEFAULT_THUMBNAIL_CONTENT_TYPE))
            .andExpect(jsonPath("$.thumbnail").value(Base64.getEncoder().encodeToString(DEFAULT_THUMBNAIL)));
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
        updatedAlbum
            .name(UPDATED_NAME)
            .event(UPDATED_EVENT)
            .creationDate(UPDATED_CREATION_DATE)
            .overrideDate(UPDATED_OVERRIDE_DATE)
            .thumbnail(UPDATED_THUMBNAIL)
            .thumbnailContentType(UPDATED_THUMBNAIL_CONTENT_TYPE);
        AlbumDTO albumDTO = albumMapper.toDto(updatedAlbum);

        restAlbumMockMvc
            .perform(
                put(ENTITY_API_URL_ID, albumDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO))
            )
            .andExpect(status().isOk());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAlbumToMatchAllProperties(updatedAlbum);
    }

    @Test
    @Transactional
    void putNonExistingAlbum() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        album.setId(longCount.incrementAndGet());

        // Create the Album
        AlbumDTO albumDTO = albumMapper.toDto(album);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlbumMockMvc
            .perform(
                put(ENTITY_API_URL_ID, albumDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAlbum() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        album.setId(longCount.incrementAndGet());

        // Create the Album
        AlbumDTO albumDTO = albumMapper.toDto(album);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlbumMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(albumDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAlbum() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        album.setId(longCount.incrementAndGet());

        // Create the Album
        AlbumDTO albumDTO = albumMapper.toDto(album);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlbumMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(albumDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAlbumWithPatch() throws Exception {
        // Initialize the database
        insertedAlbum = albumRepository.saveAndFlush(album);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the album using partial update
        Album partialUpdatedAlbum = new Album();
        partialUpdatedAlbum.setId(album.getId());

        partialUpdatedAlbum
            .overrideDate(UPDATED_OVERRIDE_DATE)
            .thumbnail(UPDATED_THUMBNAIL)
            .thumbnailContentType(UPDATED_THUMBNAIL_CONTENT_TYPE);

        restAlbumMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlbum.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlbum))
            )
            .andExpect(status().isOk());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlbumUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAlbum, album), getPersistedAlbum(album));
    }

    @Test
    @Transactional
    void fullUpdateAlbumWithPatch() throws Exception {
        // Initialize the database
        insertedAlbum = albumRepository.saveAndFlush(album);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the album using partial update
        Album partialUpdatedAlbum = new Album();
        partialUpdatedAlbum.setId(album.getId());

        partialUpdatedAlbum
            .name(UPDATED_NAME)
            .event(UPDATED_EVENT)
            .creationDate(UPDATED_CREATION_DATE)
            .overrideDate(UPDATED_OVERRIDE_DATE)
            .thumbnail(UPDATED_THUMBNAIL)
            .thumbnailContentType(UPDATED_THUMBNAIL_CONTENT_TYPE);

        restAlbumMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAlbum.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAlbum))
            )
            .andExpect(status().isOk());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAlbumUpdatableFieldsEquals(partialUpdatedAlbum, getPersistedAlbum(partialUpdatedAlbum));
    }

    @Test
    @Transactional
    void patchNonExistingAlbum() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        album.setId(longCount.incrementAndGet());

        // Create the Album
        AlbumDTO albumDTO = albumMapper.toDto(album);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAlbumMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, albumDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(albumDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAlbum() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        album.setId(longCount.incrementAndGet());

        // Create the Album
        AlbumDTO albumDTO = albumMapper.toDto(album);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlbumMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(albumDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAlbum() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        album.setId(longCount.incrementAndGet());

        // Create the Album
        AlbumDTO albumDTO = albumMapper.toDto(album);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAlbumMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(albumDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Album in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
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

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return albumRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Album getPersistedAlbum(Album album) {
        return albumRepository.findById(album.getId()).orElseThrow();
    }

    protected void assertPersistedAlbumToMatchAllProperties(Album expectedAlbum) {
        assertAlbumAllPropertiesEquals(expectedAlbum, getPersistedAlbum(expectedAlbum));
    }

    protected void assertPersistedAlbumToMatchUpdatableProperties(Album expectedAlbum) {
        assertAlbumAllUpdatablePropertiesEquals(expectedAlbum, getPersistedAlbum(expectedAlbum));
    }
}
