package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AlbumTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
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
}
