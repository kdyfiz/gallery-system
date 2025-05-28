package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Album;
import java.time.Instant;
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

    // Search methods for user story requirements
    @Query(
        "select album from Album album where " +
        "(:keyword is null or lower(album.name) like lower(concat('%', :keyword, '%')) or " +
        "lower(album.keywords) like lower(concat('%', :keyword, '%')) or " +
        "lower(cast(album.description as string)) like lower(concat('%', :keyword, '%')))"
    )
    Page<Album> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("select album from Album album where " + "(:event is null or lower(album.event) like lower(concat('%', :event, '%')))")
    Page<Album> findByEvent(@Param("event") String event, Pageable pageable);

    @Query(
        "select album from Album album where " + "(:year is null or YEAR(album.creationDate) = :year or YEAR(album.overrideDate) = :year)"
    )
    Page<Album> findByYear(@Param("year") Integer year, Pageable pageable);

    @Query(
        "select distinct album from Album album left join album.tags tag where " +
        "(:tagName is null or lower(tag.name) like lower(concat('%', :tagName, '%')))"
    )
    Page<Album> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("select album from Album album where " + "(:contributorLogin is null or album.user.login = :contributorLogin)")
    Page<Album> findByContributor(@Param("contributorLogin") String contributorLogin, Pageable pageable);

    // Combined search and filter method
    @Query(
        "select distinct album from Album album left join album.tags tag where " +
        "(:keyword is null or lower(album.name) like lower(concat('%', :keyword, '%')) or " +
        "lower(album.keywords) like lower(concat('%', :keyword, '%')) or " +
        "lower(cast(album.description as string)) like lower(concat('%', :keyword, '%'))) and " +
        "(:event is null or lower(album.event) like lower(concat('%', :event, '%'))) and " +
        "(:year is null or YEAR(album.creationDate) = :year or YEAR(album.overrideDate) = :year) and " +
        "(:tagName is null or lower(tag.name) like lower(concat('%', :tagName, '%'))) and " +
        "(:contributorLogin is null or album.user.login = :contributorLogin)"
    )
    Page<Album> findBySearchAndFilters(
        @Param("keyword") String keyword,
        @Param("event") String event,
        @Param("year") Integer year,
        @Param("tagName") String tagName,
        @Param("contributorLogin") String contributorLogin,
        Pageable pageable
    );

    // Sorting methods for user story requirements
    @Query(
        "select album from Album album order by " +
        "case when album.event is null or album.event = '' then 'Miscellaneous' else album.event end, " +
        "album.name"
    )
    Page<Album> findAllOrderByEventThenName(Pageable pageable);

    @Query("select album from Album album order by " + "coalesce(album.overrideDate, album.creationDate) desc")
    Page<Album> findAllOrderByDateDesc(Pageable pageable);

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
}
