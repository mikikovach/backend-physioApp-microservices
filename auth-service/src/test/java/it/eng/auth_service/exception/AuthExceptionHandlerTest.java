package it.eng.auth_service.exception;

import it.eng.auth_service.dto.ErrorResponse;
import it.eng.auth_service.dto.LoginUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AuthExceptionHandlerTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    private final AuthExceptionHandler authExceptionHandler = new AuthExceptionHandler();

    @Test
    @DisplayName("Returns stable 400 validation payload with machine-readable field errors")
    void handleMethodArgumentNotValid_ShouldReturnValidationErrorPayload_WhenDtoValidationFails() throws NoSuchMethodException {
        Method method = AuthExceptionHandlerTest.class.getDeclaredMethod("sampleValidatedMethod", LoginUserDto.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        LoginUserDto loginUserDto = new LoginUserDto("", "");

        BindingResult bindingResult = new BeanPropertyBindingResult(loginUserDto, "loginUserDto");
        bindingResult.addError(new FieldError("loginUserDto", "email", "email is required"));
        bindingResult.addError(new FieldError("loginUserDto", "password", "password is required"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ErrorResponse errorResponse = authExceptionHandler.handleMethodArgumentNotValid(exception, httpServletRequest);

        assertEquals("VALIDATION_ERROR", errorResponse.errorMessage());
        Map<String, String> expectedFieldErrors = Map.of(
                "email", "email is required",
                "password", "password is required");
        assertEquals(expectedFieldErrors, errorResponse.fieldErrors());
    }

    @Test
    @DisplayName("Validation payload does not expose stack trace details")
    void handleMethodArgumentNotValid_ShouldNotExposeInternalDetails_WhenValidationFails() throws NoSuchMethodException {
        Method method = AuthExceptionHandlerTest.class.getDeclaredMethod("sampleValidatedMethod", LoginUserDto.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        BindingResult bindingResult = new BeanPropertyBindingResult(new LoginUserDto("", ""), "loginUserDto");
        bindingResult.addError(new FieldError("loginUserDto", "email", "email must be valid"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ErrorResponse errorResponse = authExceptionHandler.handleMethodArgumentNotValid(exception, httpServletRequest);

        assertTrue(errorResponse.fieldErrors().values().stream().noneMatch(value -> value.contains("java.")));
        assertTrue(errorResponse.fieldErrors().values().stream().noneMatch(value -> value.contains("Exception")));
    }

    private void sampleValidatedMethod(@Valid LoginUserDto loginUserDto) {
        // Reflection-only helper for constructing MethodArgumentNotValidException in unit tests.
    }
}

