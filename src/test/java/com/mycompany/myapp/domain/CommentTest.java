package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.AlbumTestSamples.*;
import static com.mycompany.myapp.domain.CommentTestSamples.*;
import static com.mycompany.myapp.domain.PhotoTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Comment.class);
        Comment comment1 = getCommentSample1();
        Comment comment2 = new Comment();
        assertThat(comment1).isNotEqualTo(comment2);

        comment2.setId(comment1.getId());
        assertThat(comment1).isEqualTo(comment2);

        comment2 = getCommentSample2();
        assertThat(comment1).isNotEqualTo(comment2);
    }

    @Test
    void albumTest() {
        Comment comment = getCommentRandomSampleGenerator();
        Album albumBack = getAlbumRandomSampleGenerator();

        comment.setAlbum(albumBack);
        assertThat(comment.getAlbum()).isEqualTo(albumBack);

        comment.album(null);
        assertThat(comment.getAlbum()).isNull();
    }

    @Test
    void photoTest() {
        Comment comment = getCommentRandomSampleGenerator();
        Photo photoBack = getPhotoRandomSampleGenerator();

        comment.setPhoto(photoBack);
        assertThat(comment.getPhoto()).isEqualTo(photoBack);

        comment.photo(null);
        assertThat(comment.getPhoto()).isNull();
    }
}
