package it.eng.reservations_service.controller;

import it.eng.reservations_service.dto.ReservationViewDTO;
import it.eng.reservations_service.exception.ReservationsExceptionHandler;
import it.eng.reservations_service.service.ReservationsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationController tests")
class ReservationControllerTest {

    private static final String USER_ID_HEADER_NAME = "X-User-Id";
    private static final String RESERVATIONS_PATH = "/reservations";

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validatorFactoryBean;

    @Mock
    private ReservationsService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();

        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validatorFactoryBean);
        methodValidationPostProcessor.afterPropertiesSet();

        Object validatedController = methodValidationPostProcessor
                .postProcessAfterInitialization(reservationController, "reservationController");

        mockMvc = MockMvcBuilders.standaloneSetup(validatedController)
                .setControllerAdvice(new ReservationsExceptionHandler())
                .setValidator(validatorFactoryBean)
                .build();
    }

    @AfterEach
    void tearDown() {
        validatorFactoryBean.close();
    }

    @Test
    @DisplayName("DELETE rejects non-positive reservation ID before service invocation")
    void cancelReservationShouldRejectNonPositiveId() throws Exception {
        mockMvc.perform(delete("/reservations/0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("DELETE accepts positive reservation ID and invokes the service")
    void cancelReservationShouldAcceptPositiveId() throws Exception {
        mockMvc.perform(delete("/reservations/12"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(12L);
    }

    @Test
    @DisplayName("GET my reservations rejects missing user header before service invocation")
    void getMyReservationsShouldRejectMissingUserHeader() throws Exception {
        mockMvc.perform(get("/reservations/my-reservations"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("GET my reservations rejects non-numeric user header before service invocation")
    void getMyReservationsShouldRejectNonNumericUserHeader() throws Exception {
        mockMvc.perform(get("/reservations/my-reservations")
                        .header("X-User-Id", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("GET my reservations accepts a valid user header and returns reservations")
    void getMyReservationsShouldAcceptValidUserHeader() throws Exception {
        ReservationViewDTO reservationView = new ReservationViewDTO(
                5L,
                8L,
                11L,
                13L,
                "Alice",
                "Physio",
                LocalDateTime.of(2026, 8, 31, 10, 30)
        );
        when(reservationService.getMyReservations(11L)).thenReturn(List.of(reservationView));

        String responseBody = mockMvc.perform(get("/reservations/my-reservations")
                        .header("X-User-Id", "11"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(reservationService).getMyReservations(11L);
        assertThat(responseBody)
                .contains("Alice")
                .contains("11");
    }

    @Test
    @DisplayName("POST create rejects missing user header before service invocation")
    void createReservationShouldRejectMissingUserHeader() throws Exception {
        mockMvc.perform(post(RESERVATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"slotId\":42" + "}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @Test
    @DisplayName("POST create rejects overflow user header before service invocation")
    void createReservationShouldRejectOverflowUserHeader() throws Exception {
        mockMvc.perform(post(RESERVATIONS_PATH)
                        .header(USER_ID_HEADER_NAME, "999999999999999999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"slotId\":42" + "}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reservationService);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateReservationPayloads")
    @DisplayName("POST create rejects invalid slotId payloads before service invocation")
    void createReservationShouldRejectInvalidPayload(String requestBody, String expectedMessage) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post(RESERVATIONS_PATH)
                        .header(USER_ID_HEADER_NAME, "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        verifyNoInteractions(reservationService);
        assertThat(responseBody)
                .contains("fieldErrors")
                .contains("slotId")
                .contains(expectedMessage);
    }

    @Test
    @DisplayName("POST create accepts a valid user header and invokes the service")
    void createReservationShouldAcceptValidUserHeader() throws Exception {
        when(reservationService.createReservation(7L, 42L)).thenReturn(Mono.empty());

        mockMvc.perform(post(RESERVATIONS_PATH)
                        .header(USER_ID_HEADER_NAME, "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" + "\"slotId\":42" + "}"))
                .andExpect(status().isCreated());

        verify(reservationService).createReservation(7L, 42L);
    }

    private static Stream<Arguments> invalidCreateReservationPayloads() {
        return Stream.of(
                Arguments.of("{}", "Slot ID is required"),
                Arguments.of("{\"slotId\":null}", "Slot ID is required"),
                Arguments.of("{\"slotId\":0}", "Slot ID must be a positive number"),
                Arguments.of("{\"slotId\":-42}", "Slot ID must be a positive number")
        );
    }
}


