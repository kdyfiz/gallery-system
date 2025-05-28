package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AlbumTestSamples.*;
import static com.mycompany.myapp.domain.TagTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
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
    void tagsTest() {
        Album album = getAlbumRandomSampleGenerator();
        Tag tagBack = getTagRandomSampleGenerator();

        album.addTags(tagBack);
        assertThat(album.getTags()).containsOnly(tagBack);

        album.removeTags(tagBack);
        assertThat(album.getTags()).doesNotContain(tagBack);

        album.tags(new HashSet<>(Set.of(tagBack)));
        assertThat(album.getTags()).containsOnly(tagBack);

        album.setTags(new HashSet<>());
        assertThat(album.getTags()).doesNotContain(tagBack);
    }
}
