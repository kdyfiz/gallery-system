package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.PhotoAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Photo;
import com.mycompany.myapp.repository.PhotoRepository;
import com.mycompany.myapp.service.PhotoService;
import com.mycompany.myapp.service.dto.PhotoDTO;
import com.mycompany.myapp.service.mapper.PhotoMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
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
 * Integration tests for the {@link PhotoResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PhotoResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final byte[] DEFAULT_IMAGE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_CONTENT_TYPE = "image/png";

    private static final Instant DEFAULT_UPLOAD_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPLOAD_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_CAPTURE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CAPTURE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_LOCATION = "AAAAAAAAAA";
    private static final String UPDATED_LOCATION = "BBBBBBBBBB";

    private static final String DEFAULT_KEYWORDS = "AAAAAAAAAA";
    private static final String UPDATED_KEYWORDS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/photos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PhotoRepository photoRepository;

    @Mock
    private PhotoRepository photoRepositoryMock;

    @Autowired
    private PhotoMapper photoMapper;

    @Mock
    private PhotoService photoServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPhotoMockMvc;

    private Photo photo;

    private Photo insertedPhoto;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Photo createEntity() {
        return new Photo()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .image(DEFAULT_IMAGE)
            .imageContentType(DEFAULT_IMAGE_CONTENT_TYPE)
            .uploadDate(DEFAULT_UPLOAD_DATE)
            .captureDate(DEFAULT_CAPTURE_DATE)
            .location(DEFAULT_LOCATION)
            .keywords(DEFAULT_KEYWORDS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Photo createUpdatedEntity() {
        return new Photo()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .uploadDate(UPDATED_UPLOAD_DATE)
            .captureDate(UPDATED_CAPTURE_DATE)
            .location(UPDATED_LOCATION)
            .keywords(UPDATED_KEYWORDS);
    }

    @BeforeEach
    void initTest() {
        photo = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPhoto != null) {
            photoRepository.delete(insertedPhoto);
            insertedPhoto = null;
        }
    }

    @Test
    @Transactional
    void createPhoto() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Photo
        PhotoDTO photoDTO = photoMapper.toDto(photo);
        var returnedPhotoDTO = om.readValue(
            restPhotoMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(photoDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PhotoDTO.class
        );

        // Validate the Photo in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPhoto = photoMapper.toEntity(returnedPhotoDTO);
        assertPhotoUpdatableFieldsEquals(returnedPhoto, getPersistedPhoto(returnedPhoto));

        insertedPhoto = returnedPhoto;
    }

    @Test
    @Transactional
    void createPhotoWithExistingId() throws Exception {
        // Create the Photo with an existing ID
        photo.setId(1L);
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPhotoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(photoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUploadDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        photo.setUploadDate(null);

        // Create the Photo, which fails.
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        restPhotoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(photoDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPhotos() throws Exception {
        // Initialize the database
        insertedPhoto = photoRepository.saveAndFlush(photo);

        // Get all the photoList
        restPhotoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(photo.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_IMAGE))))
            .andExpect(jsonPath("$.[*].uploadDate").value(hasItem(DEFAULT_UPLOAD_DATE.toString())))
            .andExpect(jsonPath("$.[*].captureDate").value(hasItem(DEFAULT_CAPTURE_DATE.toString())))
            .andExpect(jsonPath("$.[*].location").value(hasItem(DEFAULT_LOCATION)))
            .andExpect(jsonPath("$.[*].keywords").value(hasItem(DEFAULT_KEYWORDS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPhotosWithEagerRelationshipsIsEnabled() throws Exception {
        when(photoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPhotoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(photoServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllPhotosWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(photoServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restPhotoMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(photoRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getPhoto() throws Exception {
        // Initialize the database
        insertedPhoto = photoRepository.saveAndFlush(photo);

        // Get the photo
        restPhotoMockMvc
            .perform(get(ENTITY_API_URL_ID, photo.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(photo.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.imageContentType").value(DEFAULT_IMAGE_CONTENT_TYPE))
            .andExpect(jsonPath("$.image").value(Base64.getEncoder().encodeToString(DEFAULT_IMAGE)))
            .andExpect(jsonPath("$.uploadDate").value(DEFAULT_UPLOAD_DATE.toString()))
            .andExpect(jsonPath("$.captureDate").value(DEFAULT_CAPTURE_DATE.toString()))
            .andExpect(jsonPath("$.location").value(DEFAULT_LOCATION))
            .andExpect(jsonPath("$.keywords").value(DEFAULT_KEYWORDS));
    }

    @Test
    @Transactional
    void getNonExistingPhoto() throws Exception {
        // Get the photo
        restPhotoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPhoto() throws Exception {
        // Initialize the database
        insertedPhoto = photoRepository.saveAndFlush(photo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the photo
        Photo updatedPhoto = photoRepository.findById(photo.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPhoto are not directly saved in db
        em.detach(updatedPhoto);
        updatedPhoto
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .uploadDate(UPDATED_UPLOAD_DATE)
            .captureDate(UPDATED_CAPTURE_DATE)
            .location(UPDATED_LOCATION)
            .keywords(UPDATED_KEYWORDS);
        PhotoDTO photoDTO = photoMapper.toDto(updatedPhoto);

        restPhotoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, photoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(photoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPhotoToMatchAllProperties(updatedPhoto);
    }

    @Test
    @Transactional
    void putNonExistingPhoto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        photo.setId(longCount.incrementAndGet());

        // Create the Photo
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPhotoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, photoDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(photoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPhoto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        photo.setId(longCount.incrementAndGet());

        // Create the Photo
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPhotoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(photoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPhoto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        photo.setId(longCount.incrementAndGet());

        // Create the Photo
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPhotoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(photoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePhotoWithPatch() throws Exception {
        // Initialize the database
        insertedPhoto = photoRepository.saveAndFlush(photo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the photo using partial update
        Photo partialUpdatedPhoto = new Photo();
        partialUpdatedPhoto.setId(photo.getId());

        partialUpdatedPhoto.description(UPDATED_DESCRIPTION).keywords(UPDATED_KEYWORDS);

        restPhotoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPhoto.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPhoto))
            )
            .andExpect(status().isOk());

        // Validate the Photo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPhotoUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPhoto, photo), getPersistedPhoto(photo));
    }

    @Test
    @Transactional
    void fullUpdatePhotoWithPatch() throws Exception {
        // Initialize the database
        insertedPhoto = photoRepository.saveAndFlush(photo);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the photo using partial update
        Photo partialUpdatedPhoto = new Photo();
        partialUpdatedPhoto.setId(photo.getId());

        partialUpdatedPhoto
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .uploadDate(UPDATED_UPLOAD_DATE)
            .captureDate(UPDATED_CAPTURE_DATE)
            .location(UPDATED_LOCATION)
            .keywords(UPDATED_KEYWORDS);

        restPhotoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPhoto.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPhoto))
            )
            .andExpect(status().isOk());

        // Validate the Photo in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPhotoUpdatableFieldsEquals(partialUpdatedPhoto, getPersistedPhoto(partialUpdatedPhoto));
    }

    @Test
    @Transactional
    void patchNonExistingPhoto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        photo.setId(longCount.incrementAndGet());

        // Create the Photo
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPhotoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, photoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(photoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPhoto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        photo.setId(longCount.incrementAndGet());

        // Create the Photo
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPhotoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(photoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPhoto() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        photo.setId(longCount.incrementAndGet());

        // Create the Photo
        PhotoDTO photoDTO = photoMapper.toDto(photo);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPhotoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(photoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Photo in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePhoto() throws Exception {
        // Initialize the database
        insertedPhoto = photoRepository.saveAndFlush(photo);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the photo
        restPhotoMockMvc
            .perform(delete(ENTITY_API_URL_ID, photo.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return photoRepository.count();
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

    protected Photo getPersistedPhoto(Photo photo) {
        return photoRepository.findById(photo.getId()).orElseThrow();
    }

    protected void assertPersistedPhotoToMatchAllProperties(Photo expectedPhoto) {
        assertPhotoAllPropertiesEquals(expectedPhoto, getPersistedPhoto(expectedPhoto));
    }

    protected void assertPersistedPhotoToMatchUpdatableProperties(Photo expectedPhoto) {
        assertPhotoAllUpdatablePropertiesEquals(expectedPhoto, getPersistedPhoto(expectedPhoto));
    }
}
