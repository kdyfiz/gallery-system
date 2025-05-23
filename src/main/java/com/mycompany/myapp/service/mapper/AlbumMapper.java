package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Album;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.service.dto.AlbumDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Album} and its DTO {@link AlbumDTO}.
 */
@Mapper(componentModel = "spring")
public interface AlbumMapper extends EntityMapper<AlbumDTO, Album> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    AlbumDTO toDto(Album s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
