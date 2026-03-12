package it.eng.auth_service.service;


import it.eng.auth_service.dto.LoginUserDto;
import it.eng.auth_service.dto.UserDTO;
import it.eng.auth_service.entity.User;
import it.eng.auth_service.exception.EmailAlreadyExistException;
import it.eng.auth_service.exception.InvalidCredentialsException;
import it.eng.auth_service.mapper.UserMapper;
import it.eng.auth_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import it.eng.auth_service.util.LoginResponse;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User signup(UserDTO input) {

        User user = userMapper.toEntity(input);
        user.setPassword(passwordEncoder.encode(input.password()));

         if (userRepository.findByEmail(user.getEmail()).isPresent()) {
             throw new EmailAlreadyExistException("Email already in use");
         }

        return userRepository.save(user);
    }


    public LoginResponse buildLoginresponse(User authenticatedUser, String jwtToken, Long expirationTime) {
        return LoginResponse.builder()
                .token(jwtToken).expiresIn(expirationTime)
                .email(authenticatedUser.getEmail())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .userId(authenticatedUser.getUserId()).build();
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
