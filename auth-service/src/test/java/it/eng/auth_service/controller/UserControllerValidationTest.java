package it.eng.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.eng.auth_service.dto.UpdateUserDTO;
import it.eng.auth_service.dto.UserResponseDTO;
import it.eng.auth_service.exception.AuthExceptionHandler;
import it.eng.auth_service.service.UserService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerValidationTest {

    private static final String UPDATE_PATH = "/users/edit";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LocalValidatorFactoryBean validatorFactoryBean;

    @BeforeEach
    void setUp() {
        validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
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
    @DisplayName("Update user returns HTTP 400 and field errors when payload is invalid")
    void updateUser_ShouldReturnBadRequest_WhenPayloadIsInvalid() throws Exception {
        UpdateUserDTO invalidUpdatePayload = new UpdateUserDTO(
                "",
                "",
                "invalid-email",
                LocalDate.now().plusDays(1),
                "",
                "",
                0L);

        mockMvc.perform(put(UPDATE_PATH)
                        .header(USER_EMAIL_HEADER, "john.doe@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdatePayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.lastName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.birthDate").exists())
                .andExpect(jsonPath("$.fieldErrors.street").exists())
                .andExpect(jsonPath("$.fieldErrors.city").exists())
                .andExpect(jsonPath("$.fieldErrors.postalCode").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Update user returns HTTP 200 when payload is valid")
    void updateUser_ShouldReturnOk_WhenPayloadIsValid() throws Exception {
        String authenticatedEmail = "john.doe@example.com";
        Long authenticatedUserId = 7L;
        UpdateUserDTO validUpdatePayload = new UpdateUserDTO(
                "John",
                "Doe",
                authenticatedEmail,
                LocalDate.of(1990, 1, 1),
                "Main Street 1",
                "Belgrade",
                11000L);

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                authenticatedUserId,
                "John",
                "Doe",
                authenticatedEmail,
                LocalDate.of(1990, 1, 1),
                "Main Street 1",
                "Belgrade",
                11000L);

        when(userService.getUserIdByUsername(authenticatedEmail)).thenReturn(authenticatedUserId);
        when(userService.editUser(authenticatedUserId, validUpdatePayload)).thenReturn(userResponseDTO);

        mockMvc.perform(put(UPDATE_PATH)
                        .header(USER_EMAIL_HEADER, authenticatedEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(authenticatedUserId))
                .andExpect(jsonPath("$.email").value(authenticatedEmail));

        verify(userService).getUserIdByUsername(authenticatedEmail);
        verify(userService).editUser(authenticatedUserId, validUpdatePayload);
    }
}

