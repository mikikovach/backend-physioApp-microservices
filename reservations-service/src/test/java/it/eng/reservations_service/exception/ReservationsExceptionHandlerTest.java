package it.eng.reservations_service.exception;

import it.eng.reservations_service.util.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationsExceptionHandler Tests")
class ReservationsExceptionHandlerTest {

    private ReservationsExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ReservationsExceptionHandler();
    }

    @Test
    @DisplayName("Should map body validation errors to BAD_REQUEST with fieldErrors")
    void handleMethodArgumentNotValid_ShouldReturnFieldErrors() {
        when(httpServletRequest.getRequestURI()).thenReturn("/reservations");
        List<FieldError> fieldErrors = List.of(
                new FieldError("ReservationRequestDTO", "slotId", "Slot ID is required")
        );
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiError> response = exceptionHandler.handleMethodArgumentNotValid(exception, httpServletRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getFieldErrors()).containsEntry("slotId", "Slot ID is required");
    }

    @Test
    @DisplayName("Should map null field-error message to default text")
    void handleMethodArgumentNotValid_ShouldUseDefaultMessage() {
        when(httpServletRequest.getRequestURI()).thenReturn("/reservations");
        List<FieldError> fieldErrors = List.of(
                new FieldError("ReservationRequestDTO", "slotId", null)
        );
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiError> response = exceptionHandler.handleMethodArgumentNotValid(exception, httpServletRequest);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFieldErrors()).containsEntry("slotId", "Invalid value");
    }

    @Test
    @DisplayName("Should map parameter constraint violations to BAD_REQUEST")
    void handleConstraintViolation_ShouldReturnFieldErrors() {
        when(httpServletRequest.getRequestURI()).thenReturn("/reservations");
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("cancelReservation.id");

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("Reservation ID must be a positive number");

        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        when(exception.getConstraintViolations()).thenReturn(Set.of(violation));

        ResponseEntity<ApiError> response = exceptionHandler.handleConstraintViolation(exception, httpServletRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFieldErrors())
                .containsEntry("cancelReservation.id", "Reservation ID must be a positive number");
    }

    @Test
    @DisplayName("Should preserve NOT_FOUND mapping for reservation domain errors")
    void handleReservationNotFoundException_ShouldReturnNotFound() {
        when(httpServletRequest.getRequestURI()).thenReturn("/reservations");
        ReservationNotFoundException exception = new ReservationNotFoundException("Reservation not found");

        ResponseEntity<ApiError> response = exceptionHandler.handleReservationNotFoundException(exception, httpServletRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Reservation not found");
        assertThat(response.getBody().getPath()).isEqualTo("/reservations");
    }

    @Test
    @DisplayName("Should preserve CONFLICT mapping for already reserved slot")
    void handleSlotAlreadyReserved_ShouldReturnConflict() {
        SlotAlreadyReservedInReservationContextException exception =
                new SlotAlreadyReservedInReservationContextException("Slot already reserved");

        ResponseEntity<ApiError> response = exceptionHandler.handleSlotAlreadyReserved(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/slots/reserve");
    }

    @Test
    @DisplayName("Should preserve BAD_REQUEST mapping for slot client errors")
    void handleSlotClientException_ShouldReturnBadRequest() {
        when(httpServletRequest.getRequestURI()).thenReturn("/reservations");
        SlotClientException exception = new SlotClientException("Invalid slot request");

        ResponseEntity<ApiError> response = exceptionHandler.handleSlotClientException(exception, httpServletRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid slot request");
    }

    @Test
    @DisplayName("Should include timestamp in error payload")
    void errorPayload_ShouldContainTimestamp() {
        when(httpServletRequest.getRequestURI()).thenReturn("/reservations");
        ReservationNotFoundException exception = new ReservationNotFoundException("Reservation not found");

        ResponseEntity<ApiError> response = exceptionHandler.handleReservationNotFoundException(exception, httpServletRequest);
        LocalDateTime timestamp = response.getBody() == null ? null : response.getBody().getTimestamp();

        assertThat(timestamp).isNotNull();
    }

    @Test
    @DisplayName("Should return 503 for SlotServiceUnavailableException")
    void handleSlotServiceUnavailable_ShouldReturn503() {
        when(httpServletRequest.getRequestURI()).thenReturn("/reservations");
        SlotServiceUnavailableException exception = new SlotServiceUnavailableException("Slot service unavailable");

        ResponseEntity<ApiError> response = exceptionHandler.handleSlotServiceUnavailable(exception, httpServletRequest);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Slot service unavailable");
        assertThat(response.getBody().getPath()).isEqualTo("/reservations");
    }

    @Test
    @DisplayName("Should pass through downstream status and body without stack trace for WebClientResponseException")
    void handleWebClientException_ShouldPassThroughStatusAndBody() {
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                null,
                "Downstream not found body".getBytes(),
                null
        );

        ResponseEntity<String> response = exceptionHandler.handleWebClientException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isEqualTo("Downstream not found body");
        assertThat(response.getBody()).doesNotContain("at it.eng");
    }

    @Test
    @DisplayName("Should pass through 409 status for WebClientResponseException conflict")
    void handleWebClientException_ShouldPassThroughConflictStatus() {
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                null,
                "Slot conflict".getBytes(),
                null
        );

        ResponseEntity<String> response = exceptionHandler.handleWebClientException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isEqualTo("Slot conflict");
    }
}

