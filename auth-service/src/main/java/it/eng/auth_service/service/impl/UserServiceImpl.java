package it.eng.auth_service.service.impl;

import it.eng.auth_service.dto.UpdateUserDTO;
import it.eng.auth_service.dto.UserDTO;
import it.eng.auth_service.dto.UserResponseDTO;
import it.eng.auth_service.entity.User;
import it.eng.auth_service.exception.NotFoundException;
import it.eng.auth_service.mapper.UserMapper;
import it.eng.auth_service.repository.UserRepository;
import it.eng.auth_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    @Override
    public UserResponseDTO getUser(Long userId) {
        User user =  userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // Implementation here
        return null;
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        // Implementation here
        return null;
    }

    @Override
    public void deleteUser(String userId) {
        // Implementation here
    }

    @Override
    public List<UserDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public Long getUserIdByUsername(String userName) {
        return userRepository.findByEmail(userName)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + userName))
                .getUserId();
    }

    @Override
    public UserResponseDTO editUser(Long userId, UpdateUserDTO updateUserDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        userMapper.updateUserFromDto(updateUserDTO, user);



        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(updatedUser);

    }
}
