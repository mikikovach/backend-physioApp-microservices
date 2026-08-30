package it.eng.appointments_service.exception;

import it.eng.appointments_service.dto.AppointmentSlotDTO;
import it.eng.appointments_service.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotsExceptionHandlerTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @Mock
    private Path propertyPath;

    private final SlotsExceptionHandler slotsExceptionHandler = new SlotsExceptionHandler();

    @Test
    @DisplayName("Returns 400 ApiError for invalid request bodies")
    void handleMethodArgumentNotValid_ShouldReturnBadRequest_WhenBodyValidationFails() throws NoSuchMethodException {
        Method method = SlotsExceptionHandlerTest.class.getDeclaredMethod("sampleValidatedMethod", AppointmentSlotDTO.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        AppointmentSlotDTO appointmentSlotDTO = new AppointmentSlotDTO(null, LocalDateTime.now(), false, null);

        BindingResult bindingResult = new BeanPropertyBindingResult(appointmentSlotDTO, "appointmentSlotDTO");
        bindingResult.addError(new FieldError("appointmentSlotDTO", "physioId", "must not be null"));
        bindingResult.addError(new FieldError("appointmentSlotDTO", "startTime", "must not be null"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        when(httpServletRequest.getRequestURI()).thenReturn("/slots/insert");

        ApiError apiError = slotsExceptionHandler.handleMethodArgumentNotValid(exception, httpServletRequest).getBody();

        assertEquals(HttpStatus.BAD_REQUEST.value(), apiError.getStatus());
        assertEquals("Bad Request", apiError.getError());
        assertEquals("physioId: must not be null; startTime: must not be null", apiError.getMessage());
        assertEquals("/slots/insert", apiError.getPath());
    }

    @Test
    @DisplayName("Returns sanitized 400 ApiError for invalid path or query params")
    void handleConstraintViolationException_ShouldReturnBadRequest_WhenPathOrQueryValidationFails() {
        when(propertyPath.toString()).thenReturn("reserveSlot.slotId");
        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(constraintViolation.getMessage()).thenReturn("must be greater than 0");
        when(httpServletRequest.getRequestURI()).thenReturn("/slots/reserve/0");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(constraintViolation));

        ApiError apiError = slotsExceptionHandler.handleConstraintViolationException(exception, httpServletRequest).getBody();

        assertEquals(HttpStatus.BAD_REQUEST.value(), apiError.getStatus());
        assertEquals("Bad Request", apiError.getError());
        assertEquals("slotId: must be greater than 0", apiError.getMessage());
        assertEquals("/slots/reserve/0", apiError.getPath());
        assertFalse(apiError.getMessage().contains("java."));
    }

    @Test
    @DisplayName("Keeps existing 404 mapping for slot not found domain exception")
    void handleSlotNotFoundException_ShouldReturnNotFound_WhenSlotDoesNotExist() {
        SlotNotFoundException exception = new SlotNotFoundException("Slot not found");
        when(httpServletRequest.getRequestURI()).thenReturn("/slots/findSlot/999");

        ApiError apiError = slotsExceptionHandler.handleSlotNotFoundException(exception, httpServletRequest).getBody();

        assertEquals(HttpStatus.NOT_FOUND.value(), apiError.getStatus());
        assertEquals("Not Found", apiError.getError());
        assertEquals("Slot not found", apiError.getMessage());
        assertEquals("/slots/findSlot/999", apiError.getPath());
    }

    private void sampleValidatedMethod(@Valid AppointmentSlotDTO appointmentSlotDTO) {
        // Reflection-only helper for constructing MethodArgumentNotValidException in unit tests.
    }
}


