package it.eng.auth_service;

import it.eng.auth_service.dto.LoginUserDto;
import it.eng.auth_service.entity.User;
import it.eng.auth_service.exception.InvalidCredentialsException;
import it.eng.auth_service.repository.UserRepository;
import it.eng.auth_service.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthenticationService authenticationService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void authenticate_ShouldThrowInvalidCredentialsException_WhenPasswordDoesNotMatch() {
		// Arrange
		String email = "test@example.com";
		String password = "password";
		String encodedPassword = "encodedPassword";

		LoginUserDto loginUserDto = new LoginUserDto(email, password);
		User user = new User();
		user.setEmail(email);
		user.setPassword(encodedPassword);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

		// Act & Assert
		assertThrows(InvalidCredentialsException.class, () -> authenticationService.authenticate(loginUserDto));
		verify(userRepository, times(1)).findByEmail(email);
		verify(passwordEncoder, times(1)).matches(password, encodedPassword);
	}

	@Test
	void authenticate_ShouldReturnUser_WhenCredentialsAreValid() {
		// Arrange
		String email = "test@example.com";
		String password = "password";
		String encodedPassword = "encodedPassword";

		LoginUserDto loginUserDto = new LoginUserDto(email, password);
		User user = new User();
		user.setEmail(email);
		user.setPassword(encodedPassword);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

		// Act
		User result = authenticationService.authenticate(loginUserDto);

		// Assert
		assertNotNull(result);
		assertEquals(email, result.getEmail());
		verify(userRepository, times(1)).findByEmail(email);
		verify(passwordEncoder, times(1)).matches(password, encodedPassword);
	}


}
