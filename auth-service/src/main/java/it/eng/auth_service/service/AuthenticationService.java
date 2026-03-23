package it.eng.auth_service.service;


import it.eng.auth_service.dto.LoginUserDto;
import it.eng.auth_service.dto.UserDTO;
import it.eng.auth_service.entity.Role;
import it.eng.auth_service.entity.User;
import it.eng.auth_service.exception.EmailAlreadyExistException;
import it.eng.auth_service.exception.InvalidCredentialsException;
import it.eng.auth_service.mapper.UserMapper;
import it.eng.auth_service.repository.RoleRepository;
import it.eng.auth_service.repository.UserRepository;
import it.eng.auth_service.util.RoleName;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import it.eng.auth_service.util.LoginResponse;

import java.util.HashSet;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public User signup(UserDTO input) {

        User user = userMapper.toEntity(input);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("Email already in use");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER.name())
                .orElseThrow(() -> new RuntimeException("User Role not found"));

        user.setPassword(passwordEncoder.encode(input.password()));
        user.getRoles().add(userRole);

        return userRepository.save(user);
    }


    public LoginResponse buildLoginresponse(User authenticatedUser, String jwtToken, Long expirationTime) {
        return LoginResponse.builder()
                .token(jwtToken).expiresIn(expirationTime)
                .email(authenticatedUser.getEmail())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .userId(authenticatedUser.getUserId())
                .roles(authenticatedUser.getRoles().stream().map(role -> role.getName().name()).toList())
                .build();

    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(input.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return user;
    }

}
