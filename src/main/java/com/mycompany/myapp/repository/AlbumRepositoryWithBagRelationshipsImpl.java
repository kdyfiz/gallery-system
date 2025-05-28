package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Album;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class AlbumRepositoryWithBagRelationshipsImpl implements AlbumRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String ALBUMS_PARAMETER = "albums";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Album> fetchBagRelationships(Optional<Album> album) {
        return album.map(this::fetchTags);
    }

    @Override
    public Page<Album> fetchBagRelationships(Page<Album> albums) {
        return new PageImpl<>(fetchBagRelationships(albums.getContent()), albums.getPageable(), albums.getTotalElements());
    }

    @Override
    public List<Album> fetchBagRelationships(List<Album> albums) {
        return Optional.of(albums).map(this::fetchTags).orElse(Collections.emptyList());
    }

    Album fetchTags(Album result) {
        return entityManager
            .createQuery("select album from Album album left join fetch album.tags where album.id = :id", Album.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<Album> fetchTags(List<Album> albums) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, albums.size()).forEach(index -> order.put(albums.get(index).getId(), index));
        List<Album> result = entityManager
            .createQuery("select album from Album album left join fetch album.tags where album in :albums", Album.class)
            .setParameter(ALBUMS_PARAMETER, albums)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
