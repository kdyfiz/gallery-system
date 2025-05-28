package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.Photo;
import com.mycompany.myapp.domain.Tag;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.dto.PhotoDTO;
import com.mycompany.myapp.service.dto.TagDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Photo} and its DTO {@link PhotoDTO}.
 */
@Mapper(componentModel = "spring")
public interface PhotoMapper extends EntityMapper<PhotoDTO, Photo> {
    @Mapping(target = "album", source = "album", qualifiedByName = "albumName")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagNameSet")
    PhotoDTO toDto(Photo s);

    @Mapping(target = "removeTags", ignore = true)
    Photo toEntity(PhotoDTO photoDTO);

    @Named("albumName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    AlbumDTO toDtoAlbumName(Album album);

    @Named("tagName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TagDTO toDtoTagName(Tag tag);

    @Named("tagNameSet")
    default Set<TagDTO> toDtoTagNameSet(Set<Tag> tag) {
        return tag.stream().map(this::toDtoTagName).collect(Collectors.toSet());
    }
}
