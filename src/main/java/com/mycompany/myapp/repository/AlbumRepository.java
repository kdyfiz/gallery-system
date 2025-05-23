package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Album;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Album entity.
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    @Query("select album from Album album where album.user.login = ?#{authentication.name}")
    List<Album> findByUserIsCurrentUser();

    default Optional<Album> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Album> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Album> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select album from Album album left join fetch album.user", countQuery = "select count(album) from Album album")
    Page<Album> findAllWithToOneRelationships(Pageable pageable);

    @Query("select album from Album album left join fetch album.user")
    List<Album> findAllWithToOneRelationships();

    @Query("select album from Album album left join fetch album.user where album.id =:id")
    Optional<Album> findOneWithToOneRelationships(@Param("id") Long id);
}
