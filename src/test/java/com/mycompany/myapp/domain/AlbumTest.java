package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AlbumTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class AlbumTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Album.class);
        Album album1 = getAlbumSample1();
        Album album2 = new Album();
        assertThat(album1).isNotEqualTo(album2);

        album2.setId(album1.getId());
        assertThat(album1).isEqualTo(album2);

        album2 = getAlbumSample2();
        assertThat(album1).isNotEqualTo(album2);
    }

    @Test
    void testAlbumBusinessLogic() {
        Album album = new Album();
        album.setName("Test Album");
        album.setEvent("Test Event");
        album.setCreationDate(Instant.now());

        assertThat(album.getName()).isEqualTo("Test Album");
        assertThat(album.getEvent()).isEqualTo("Test Event");
        assertThat(album.getCreationDate()).isNotNull();
    }

    @Test
    void testAlbumWithoutEvent() {
        Album album = new Album();
        album.setName("Album Without Event");
        album.setCreationDate(Instant.now());

        assertThat(album.getEvent()).isNull();
        assertThat(album.getName()).isEqualTo("Album Without Event");
    }

    @Test
    void testAlbumWithOverrideDate() {
        Instant creationDate = Instant.now().minus(10, ChronoUnit.DAYS);
        Instant overrideDate = Instant.now();

        Album album = new Album();
        album.setName("Album With Override Date");
        album.setCreationDate(creationDate);
        album.setOverrideDate(overrideDate);

        assertThat(album.getCreationDate()).isEqualTo(creationDate);
        assertThat(album.getOverrideDate()).isEqualTo(overrideDate);
        assertThat(album.getOverrideDate()).isAfter(album.getCreationDate());
    }

    @Test
    void testAlbumWithThumbnail() {
        Album album = new Album();
        album.setName("Album With Thumbnail");
        album.setCreationDate(Instant.now());

        byte[] thumbnailData = "test thumbnail data".getBytes();
        album.setThumbnail(thumbnailData);
        album.setThumbnailContentType("image/png");

        assertThat(album.getThumbnail()).isEqualTo(thumbnailData);
        assertThat(album.getThumbnailContentType()).isEqualTo("image/png");
    }

    @Test
    void testAlbumUserAssociation() {
        Album album = new Album();
        album.setName("User Album");
        album.setCreationDate(Instant.now());

        User user = new User();
        user.setLogin("testuser");
        album.setUser(user);

        assertThat(album.getUser()).isEqualTo(user);
        assertThat(album.getUser().getLogin()).isEqualTo("testuser");
    }
}
