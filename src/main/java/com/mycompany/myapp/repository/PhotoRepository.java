package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Photo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Photo entity.
 *
 * When extending this class, extend PhotoRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface PhotoRepository extends PhotoRepositoryWithBagRelationships, JpaRepository<Photo, Long> {
    default Optional<Photo> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Photo> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Photo> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(value = "select photo from Photo photo left join fetch photo.album", countQuery = "select count(photo) from Photo photo")
    Page<Photo> findAllWithToOneRelationships(Pageable pageable);

    @Query("select photo from Photo photo left join fetch photo.album")
    List<Photo> findAllWithToOneRelationships();

    @Query("select photo from Photo photo left join fetch photo.album where photo.id =:id")
    Optional<Photo> findOneWithToOneRelationships(@Param("id") Long id);
}
