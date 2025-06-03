package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Comment entity.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select comment from Comment comment where comment.author.login = ?#{authentication.name}")
    List<Comment> findByAuthorIsCurrentUser();

    default Optional<Comment> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Comment> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Comment> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select comment from Comment comment left join fetch comment.author left join fetch comment.album left join fetch comment.photo",
        countQuery = "select count(comment) from Comment comment"
    )
    Page<Comment> findAllWithToOneRelationships(Pageable pageable);

    @Query("select comment from Comment comment left join fetch comment.author left join fetch comment.album left join fetch comment.photo")
    List<Comment> findAllWithToOneRelationships();

    @Query(
        "select comment from Comment comment left join fetch comment.author left join fetch comment.album left join fetch comment.photo where comment.id =:id"
    )
    Optional<Comment> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Find all comments for a specific album, ordered by creation date descending.
     *
     * @param albumId the id of the album
     * @return the list of comments
     */
    @Query(
        "select comment from Comment comment left join fetch comment.author where comment.album.id = :albumId order by comment.createdDate desc"
    )
    List<Comment> findByAlbumIdOrderByCreatedDateDesc(@Param("albumId") Long albumId);

    /**
     * Find all comments for a specific photo, ordered by creation date descending.
     *
     * @param photoId the id of the photo
     * @return the list of comments
     */
    @Query(
        "select comment from Comment comment left join fetch comment.author where comment.photo.id = :photoId order by comment.createdDate desc"
    )
    List<Comment> findByPhotoIdOrderByCreatedDateDesc(@Param("photoId") Long photoId);
}
