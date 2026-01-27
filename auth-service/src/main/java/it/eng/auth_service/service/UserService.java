package it.eng.auth_service.service;

import it.eng.auth_service.dto.UpdateUserDTO;
import it.eng.auth_service.dto.UserDTO;
import it.eng.auth_service.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    public UserResponseDTO getUser(Long userId);
    public UserDTO createUser(UserDTO userDTO);
    public UserDTO updateUser(String userId, UserDTO userDTO);
    public void deleteUser(String userId);
    public List<UserDTO> getAllUsers();

    public Long getUserIdByUsername(String userName);

    public UserResponseDTO editUser(Long userId, UpdateUserDTO updateUserDTO);
}
