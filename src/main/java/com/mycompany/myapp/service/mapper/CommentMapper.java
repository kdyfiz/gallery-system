package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.Comment;
import com.mycompany.myapp.domain.Photo;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.dto.CommentDTO;
import com.mycompany.myapp.service.dto.PhotoDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Comment} and its DTO {@link CommentDTO}.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper extends EntityMapper<CommentDTO, Comment> {
    @Mapping(target = "author", source = "author", qualifiedByName = "userLogin")
    @Mapping(target = "album", source = "album", qualifiedByName = "albumName")
    @Mapping(target = "photo", source = "photo", qualifiedByName = "photoTitle")
    CommentDTO toDto(Comment s);

    @Mapping(target = "author", source = "author", qualifiedByName = "userFromId")
    @Mapping(target = "album", source = "album", qualifiedByName = "albumFromId")
    @Mapping(target = "photo", source = "photo", qualifiedByName = "photoFromId")
    Comment toEntity(CommentDTO commentDTO);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("albumName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    AlbumDTO toDtoAlbumName(Album album);

    @Named("photoTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    PhotoDTO toDtoPhotoTitle(Photo photo);

    @Named("userFromId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    User userFromId(UserDTO userDTO);

    @Named("albumFromId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Album albumFromId(AlbumDTO albumDTO);

    @Named("photoFromId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Photo photoFromId(PhotoDTO photoDTO);
}
