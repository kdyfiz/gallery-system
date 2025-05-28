package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Album;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Album entity.
 *
 * When extending this class, extend AlbumRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface AlbumRepository extends AlbumRepositoryWithBagRelationships, JpaRepository<Album, Long> {
    @Query("select album from Album album where album.user.login = ?#{authentication.name}")
    List<Album> findByUserIsCurrentUser();

    @Query("select distinct album from Album album left join album.tags tags where tags.name in :tagNames")
    Page<Album> findByTagsNameIn(@Param("tagNames") Collection<String> tagNames, Pageable pageable);

    @Query("select album from Album album where YEAR(album.creationDate) = :year")
    Page<Album> findByCreationDateYear(@Param("year") int year, Pageable pageable);

    @Query("select album from Album album where album.event like %:event%")
    Page<Album> findByEventContaining(@Param("event") String event, Pageable pageable);

    default Optional<Album> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Album> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Album> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(value = "select album from Album album left join fetch album.user", countQuery = "select count(album) from Album album")
    Page<Album> findAllWithToOneRelationships(Pageable pageable);

    @Query("select album from Album album left join fetch album.user")
    List<Album> findAllWithToOneRelationships();

    @Query("select album from Album album left join fetch album.user where album.id =:id")
    Optional<Album> findOneWithToOneRelationships(@Param("id") Long id);

    @Query("select distinct album from Album album left join fetch album.user")
    Page<Album> findAllPublic(Pageable pageable);

    @Query("select distinct album from Album album left join fetch album.user where album.user.login = ?#{principal.username}")
    Page<Album> findAllForCurrentUser(Pageable pageable);
}
