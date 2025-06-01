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
 *
 * When extending this class, extend AlbumRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface AlbumRepository extends AlbumRepositoryWithBagRelationships, JpaRepository<Album, Long> {
    @Query("select album from Album album where album.user.login = ?#{authentication.name}")
    List<Album> findByUserIsCurrentUser();

    @Query("select album from Album album left join fetch album.user left join fetch album.tags order by album.event asc")
    List<Album> findAllOrderByEventAsc();

    @Query("select album from Album album left join fetch album.user left join fetch album.tags order by album.creationDate desc")
    List<Album> findAllOrderByCreationDateDesc();

    @Query(
        """
        select distinct album from Album album
        left join fetch album.user
        left join fetch album.tags tags
        where (:keyword is null or
               lower(album.name) like lower(concat('%', :keyword, '%')) or
               lower(album.description) like lower(concat('%', :keyword, '%')) or
               lower(album.keywords) like lower(concat('%', :keyword, '%')))
        and (:event is null or lower(album.event) like lower(concat('%', :event, '%')))
        and (:year is null or year(album.creationDate) = :year)
        and (:tagName is null or exists (select t from album.tags t where lower(t.name) like lower(concat('%', :tagName, '%'))))
        and (:contributorLogin is null or lower(album.user.login) like lower(concat('%', :contributorLogin, '%')))
        order by case when :sortBy = 'DATE' then album.creationDate end desc,
                 case when :sortBy != 'DATE' then album.event end asc
        """
    )
    List<Album> searchAndFilter(
        @Param("keyword") String keyword,
        @Param("event") String event,
        @Param("year") Integer year,
        @Param("tagName") String tagName,
        @Param("contributorLogin") String contributorLogin,
        @Param("sortBy") String sortBy
    );

    @Query("select distinct album.event from Album album where album.event is not null and album.event != '' order by album.event")
    List<String> findDistinctEvents();

    @Query(
        "select distinct year(album.creationDate) from Album album where album.creationDate is not null order by year(album.creationDate) desc"
    )
    List<Integer> findDistinctYears();

    @Query("select distinct tag.name from Album album join album.tags tag where tag.name is not null order by tag.name")
    List<String> findDistinctTagNames();

    @Query("select distinct user.login from Album album join album.user user where user.login is not null order by user.login")
    List<String> findDistinctContributors();

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
