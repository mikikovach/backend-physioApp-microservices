package it.eng.auth_service.mapper;

import it.eng.auth_service.dto.UpdateUserDTO;
import it.eng.auth_service.dto.UserDTO;
import it.eng.auth_service.dto.UserResponseDTO;
import it.eng.auth_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO userDTO);
    UserResponseDTO toUserResponseDto(User user);
    List<UserDTO> toDtoList(List<User> users);
    List<User> toEntityList(List<UserDTO> userDTOs);


    User updateUserFromDto(UpdateUserDTO updateUserDTO, @MappingTarget User user);
}
