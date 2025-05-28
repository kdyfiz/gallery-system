package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AlbumTestSamples.*;
import static com.mycompany.myapp.domain.PhotoTestSamples.*;
import static com.mycompany.myapp.domain.TagTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PhotoTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Photo.class);
        Photo photo1 = getPhotoSample1();
        Photo photo2 = new Photo();
        assertThat(photo1).isNotEqualTo(photo2);

        photo2.setId(photo1.getId());
        assertThat(photo1).isEqualTo(photo2);

        photo2 = getPhotoSample2();
        assertThat(photo1).isNotEqualTo(photo2);
    }

    @Test
    void albumTest() {
        Photo photo = getPhotoRandomSampleGenerator();
        Album albumBack = getAlbumRandomSampleGenerator();

        photo.setAlbum(albumBack);
        assertThat(photo.getAlbum()).isEqualTo(albumBack);

        photo.album(null);
        assertThat(photo.getAlbum()).isNull();
    }

    @Test
    void tagsTest() {
        Photo photo = getPhotoRandomSampleGenerator();
        Tag tagBack = getTagRandomSampleGenerator();

        photo.addTags(tagBack);
        assertThat(photo.getTags()).containsOnly(tagBack);

        photo.removeTags(tagBack);
        assertThat(photo.getTags()).doesNotContain(tagBack);

        photo.tags(new HashSet<>(Set.of(tagBack)));
        assertThat(photo.getTags()).containsOnly(tagBack);

        photo.setTags(new HashSet<>());
        assertThat(photo.getTags()).doesNotContain(tagBack);
    }
}
