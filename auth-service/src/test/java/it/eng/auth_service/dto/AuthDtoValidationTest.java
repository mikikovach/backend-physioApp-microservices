package it.eng.auth_service.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("UserDTO passes validation when all signup fields are valid")
    void validateUserDto_ShouldHaveNoViolations_WhenPayloadIsValid() {
        UserDTO userDTO = new UserDTO(
                null,
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                LocalDate.of(1990, 1, 1),
                "Main Street 1",
                "Belgrade",
                11000L);

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("UserDTO fails validation for blank names, bad email and short password")
    void validateUserDto_ShouldReturnExpectedViolations_WhenPayloadIsInvalid() {
        UserDTO userDTO = new UserDTO(
                null,
                "",
                "",
                "invalid-email",
                "12345",
                null,
                "",
                "",
                null);

        Set<ConstraintViolation<UserDTO>> violations = validator.validate(userDTO);
        Map<String, String> violationMap = violations.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (firstMessage, ignoredMessage) -> firstMessage));

        assertEquals(8, violations.size());
        assertEquals("firstName is required", violationMap.get("firstName"));
        assertEquals("lastName is required", violationMap.get("lastName"));
        assertEquals("email must be valid", violationMap.get("email"));
        assertEquals("password must contain at least 8 characters", violationMap.get("password"));
        assertEquals("birthDate is required", violationMap.get("birthDate"));
        assertEquals("street is required", violationMap.get("street"));
        assertEquals("city is required", violationMap.get("city"));
        assertEquals("postalCode is required", violationMap.get("postalCode"));
    }

    @Test
    @DisplayName("LoginUserDto passes validation when email and password are valid")
    void validateLoginUserDto_ShouldHaveNoViolations_WhenPayloadIsValid() {
        LoginUserDto loginUserDto = new LoginUserDto("john.doe@example.com", "password123");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("LoginUserDto fails validation for malformed email and blank password")
    void validateLoginUserDto_ShouldReturnExpectedViolations_WhenPayloadIsInvalid() {
        LoginUserDto loginUserDto = new LoginUserDto("invalid-email", "");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);
        Map<String, String> violationMap = violations.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (firstMessage, ignoredMessage) -> firstMessage));

        assertEquals(2, violations.size());
        assertEquals("email must be valid", violationMap.get("email"));
        assertEquals("password is required", violationMap.get("password"));
    }

    @Test
    @DisplayName("UpdateUserDTO passes validation when all update fields are valid")
    void validateUpdateUserDto_ShouldHaveNoViolations_WhenPayloadIsValid() {
        UpdateUserDTO updateUserDTO = new UpdateUserDTO(
                "John",
                "Doe",
                "john.doe@example.com",
                LocalDate.of(1990, 1, 1),
                "Main Street 1",
                "Belgrade",
                11000L);

        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("UpdateUserDTO fails validation for blank names, bad email, future birth date and invalid postal code")
    void validateUpdateUserDto_ShouldReturnExpectedViolations_WhenPayloadIsInvalid() {
        UpdateUserDTO updateUserDTO = new UpdateUserDTO(
                "",
                "",
                "invalid-email",
                LocalDate.now().plusDays(1),
                "",
                "",
                0L);

        Set<ConstraintViolation<UpdateUserDTO>> violations = validator.validate(updateUserDTO);
        Map<String, String> violationMap = violations.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (firstMessage, ignoredMessage) -> firstMessage));

        assertEquals(7, violations.size());
        assertEquals("firstName is required", violationMap.get("firstName"));
        assertEquals("lastName is required", violationMap.get("lastName"));
        assertEquals("email must be valid", violationMap.get("email"));
        assertEquals("birthDate must be in the past", violationMap.get("birthDate"));
        assertEquals("street is required", violationMap.get("street"));
        assertEquals("city is required", violationMap.get("city"));
        assertEquals("postalCode must be positive", violationMap.get("postalCode"));
    }
}

