package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AlbumTestSamples.*;
import static com.mycompany.myapp.domain.PhotoTestSamples.*;
import static com.mycompany.myapp.domain.TagTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Tag.class);
        Tag tag1 = getTagSample1();
        Tag tag2 = new Tag();
        assertThat(tag1).isNotEqualTo(tag2);

        tag2.setId(tag1.getId());
        assertThat(tag1).isEqualTo(tag2);

        tag2 = getTagSample2();
        assertThat(tag1).isNotEqualTo(tag2);
    }

    @Test
    void albumsTest() {
        Tag tag = getTagRandomSampleGenerator();
        Album albumBack = getAlbumRandomSampleGenerator();

        tag.addAlbums(albumBack);
        assertThat(tag.getAlbums()).containsOnly(albumBack);
        assertThat(albumBack.getTags()).containsOnly(tag);

        tag.removeAlbums(albumBack);
        assertThat(tag.getAlbums()).doesNotContain(albumBack);
        assertThat(albumBack.getTags()).doesNotContain(tag);

        tag.albums(new HashSet<>(Set.of(albumBack)));
        assertThat(tag.getAlbums()).containsOnly(albumBack);
        assertThat(albumBack.getTags()).containsOnly(tag);

        tag.setAlbums(new HashSet<>());
        assertThat(tag.getAlbums()).doesNotContain(albumBack);
        assertThat(albumBack.getTags()).doesNotContain(tag);
    }

    @Test
    void photosTest() {
        Tag tag = getTagRandomSampleGenerator();
        Photo photoBack = getPhotoRandomSampleGenerator();

        tag.addPhotos(photoBack);
        assertThat(tag.getPhotos()).containsOnly(photoBack);
        assertThat(photoBack.getTags()).containsOnly(tag);

        tag.removePhotos(photoBack);
        assertThat(tag.getPhotos()).doesNotContain(photoBack);
        assertThat(photoBack.getTags()).doesNotContain(tag);

        tag.photos(new HashSet<>(Set.of(photoBack)));
        assertThat(tag.getPhotos()).containsOnly(photoBack);
        assertThat(photoBack.getTags()).containsOnly(tag);

        tag.setPhotos(new HashSet<>());
        assertThat(tag.getPhotos()).doesNotContain(photoBack);
        assertThat(photoBack.getTags()).doesNotContain(tag);
    }
}
