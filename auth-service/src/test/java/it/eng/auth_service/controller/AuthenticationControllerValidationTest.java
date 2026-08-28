package it.eng.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.eng.auth_service.dto.LoginUserDto;
import it.eng.auth_service.dto.UserDTO;
import it.eng.auth_service.entity.User;
import it.eng.auth_service.exception.AuthExceptionHandler;
import it.eng.auth_service.service.AuthenticationService;
import it.eng.auth_service.service.JwtService;
import it.eng.auth_service.util.LoginResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerValidationTest {

    private static final String SIGNUP_PATH = "/auth/signup";
    private static final String LOGIN_PATH = "/auth/login";

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LocalValidatorFactoryBean validatorFactoryBean;

    @BeforeEach
    void setUp() {
        validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new AuthExceptionHandler())
                .setValidator(validatorFactoryBean)
                .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @AfterEach
    void tearDown() {
        validatorFactoryBean.close();
    }

    @Test
    @DisplayName("Signup returns HTTP 400 and field errors when payload is invalid")
    void signup_ShouldReturnBadRequest_WhenPayloadIsInvalid() throws Exception {
        UserDTO invalidSignupPayload = new UserDTO(
                null,
                "",
                "",
                "invalid-email",
                "123",
                null,
                "",
                "",
                null);

        mockMvc.perform(post(SIGNUP_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSignupPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.lastName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(authenticationService, jwtService);
    }

    @Test
    @DisplayName("Signup returns HTTP 201 when payload is valid")
    void signup_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        UserDTO validSignupPayload = new UserDTO(
                null,
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                LocalDate.of(1990, 1, 1),
                "Main Street 1",
                "Belgrade",
                11000L);

        User registeredUser = new User();
        registeredUser.setUserId(7L);
        registeredUser.setFirstName("John");
        registeredUser.setLastName("Doe");
        registeredUser.setEmail("john.doe@example.com");

        String token = "jwt-token";
        Long expirationTime = 3600L;
        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .expiresIn(expirationTime)
                .userId(registeredUser.getUserId())
                .firstName(registeredUser.getFirstName())
                .lastName(registeredUser.getLastName())
                .email(registeredUser.getEmail())
                .roles(List.of("ROLE_USER"))
                .build();

        when(authenticationService.signup(validSignupPayload)).thenReturn(registeredUser);
        when(jwtService.generateToken(registeredUser)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);
        when(authenticationService.buildLoginresponse(registeredUser, token, expirationTime))
                .thenReturn(loginResponse);

        mockMvc.perform(post(SIGNUP_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignupPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(authenticationService).signup(validSignupPayload);
        verify(jwtService).generateToken(registeredUser);
        verify(jwtService).getExpirationTime();
        verify(authenticationService).buildLoginresponse(registeredUser, token, expirationTime);
    }

    @Test
    @DisplayName("Login returns HTTP 400 and field errors when payload is invalid")
    void login_ShouldReturnBadRequest_WhenPayloadIsInvalid() throws Exception {
        LoginUserDto invalidLoginPayload = new LoginUserDto("invalid-email", "");

        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        verifyNoInteractions(authenticationService, jwtService);
    }

    @Test
    @DisplayName("Login returns HTTP 200 when payload is valid")
    void login_ShouldReturnOk_WhenPayloadIsValid() throws Exception {
        LoginUserDto validLoginPayload = new LoginUserDto("john.doe@example.com", "password123");

        User authenticatedUser = new User();
        authenticatedUser.setUserId(7L);
        authenticatedUser.setFirstName("John");
        authenticatedUser.setLastName("Doe");
        authenticatedUser.setEmail("john.doe@example.com");

        String token = "jwt-token";
        Long expirationTime = 3600L;
        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .expiresIn(expirationTime)
                .userId(authenticatedUser.getUserId())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .email(authenticatedUser.getEmail())
                .roles(List.of("ROLE_USER"))
                .build();

        when(authenticationService.authenticate(validLoginPayload)).thenReturn(authenticatedUser);
        when(jwtService.generateToken(authenticatedUser)).thenReturn(token);
        when(jwtService.getExpirationTime()).thenReturn(expirationTime);
        when(authenticationService.buildLoginresponse(authenticatedUser, token, expirationTime))
                .thenReturn(loginResponse);

        mockMvc.perform(post(LOGIN_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(authenticationService).authenticate(validLoginPayload);
        verify(jwtService).generateToken(authenticatedUser);
        verify(jwtService).getExpirationTime();
        verify(authenticationService).buildLoginresponse(authenticatedUser, token, expirationTime);
    }
}


