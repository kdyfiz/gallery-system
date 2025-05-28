package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Album;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface AlbumRepositoryWithBagRelationships {
    Optional<Album> fetchBagRelationships(Optional<Album> album);

    List<Album> fetchBagRelationships(List<Album> albums);

    Page<Album> fetchBagRelationships(Page<Album> albums);
}
