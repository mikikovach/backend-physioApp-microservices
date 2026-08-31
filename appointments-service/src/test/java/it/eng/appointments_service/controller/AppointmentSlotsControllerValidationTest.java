package it.eng.appointments_service.controller;

import it.eng.appointments_service.dto.AppointmentSlotDTO;
import it.eng.appointments_service.dto.AppointmentSlotInsertRequest;
import it.eng.appointments_service.dto.SlotAvailabilityResponse;
import it.eng.appointments_service.exception.SlotsExceptionHandler;
import it.eng.appointments_service.mapper.AppointmentSlotMapper;
import it.eng.appointments_service.service.AppointmentSlotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentSlotsControllerValidationTest {

    @Mock
    private AppointmentSlotService appointmentSlotService;

    @Mock
    private AppointmentSlotMapper appointmentSlotMapper;

    @InjectMocks
    private AppointmentSlotsController appointmentSlotsController;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validatorFactoryBean;

    @BeforeEach
    void setUp() {
        validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.afterPropertiesSet();

        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validatorFactoryBean);
        methodValidationPostProcessor.afterPropertiesSet();

        Object validatedController = methodValidationPostProcessor
                .postProcessAfterInitialization(appointmentSlotsController, "appointmentSlotsController");

        mockMvc = MockMvcBuilders.standaloneSetup(validatedController)
                .setControllerAdvice(new SlotsExceptionHandler())
                .setValidator(validatorFactoryBean)
                .build();
    }

    @AfterEach
    void tearDown() {
        validatorFactoryBean.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/slots/availability/0",
            "/slots/findSlot/0"
    })
    @DisplayName("GET slotId endpoints return 400 for non-positive slotId")
    void getSlotIdEndpoints_ShouldReturnBadRequest_WhenSlotIdIsNonPositive(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(appointmentSlotService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/slots/reserve/0",
            "/slots/release/0"
    })
    @DisplayName("POST slotId endpoints return 400 for non-positive slotId")
    void postSlotIdEndpoints_ShouldReturnBadRequest_WhenSlotIdIsNonPositive(String path) throws Exception {
        mockMvc.perform(post(path))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(appointmentSlotService);
    }

    @Test
    @DisplayName("Date-based endpoint returns 400 for non-positive physioId")
    void getAvailableSlotsByPhysio_ShouldReturnBadRequest_WhenPhysioIdIsNonPositive() throws Exception {
        mockMvc.perform(get("/slots/0").queryParam("date", "2026-08-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(appointmentSlotService);
    }

    @Test
    @DisplayName("Admin date-based endpoint returns 400 for non-positive physioId")
    void getAvailableSlotsForAdmin_ShouldReturnBadRequest_WhenPhysioIdIsNonPositive() throws Exception {
        mockMvc.perform(get("/slots/admin/0").queryParam("date", "2026-08-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(appointmentSlotService);
    }

    @Test
    @DisplayName("Date-based endpoint returns 400 when date query is missing")
    void getAvailableSlotsByPhysio_ShouldReturnBadRequest_WhenDateQueryIsMissing() throws Exception {
        mockMvc.perform(get("/slots/1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(appointmentSlotService);
    }

    @Test
    @DisplayName("Admin date-based endpoint returns 400 when date query is missing")
    void getAvailableSlotsForAdmin_ShouldReturnBadRequest_WhenDateQueryIsMissing() throws Exception {
        mockMvc.perform(get("/slots/admin/1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(appointmentSlotService);
    }

    @Test
    @DisplayName("Date-based endpoint returns 400 when date query is invalid")
    void getAvailableSlotsByPhysio_ShouldReturnBadRequest_WhenDateQueryIsInvalid() throws Exception {
        mockMvc.perform(get("/slots/1").queryParam("date", "not-a-date"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(appointmentSlotService);
    }

    @Test
    @DisplayName("Admin date-based endpoint returns 400 when date query is invalid")
    void getAvailableSlotsForAdmin_ShouldReturnBadRequest_WhenDateQueryIsInvalid() throws Exception {
        mockMvc.perform(get("/slots/admin/1").queryParam("date", "not-a-date"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(appointmentSlotService);
    }

    @Test
    @DisplayName("Date-based endpoint returns 200 for valid physioId and date")
    void getAvailableSlotsByPhysio_ShouldReturnOk_WhenInputIsValid() throws Exception {
        Long physioId = 1L;
        LocalDate date = LocalDate.of(2026, 8, 30);
        List<AppointmentSlotDTO> slots = List.of();

        when(appointmentSlotService.getAvailableSlotsByTherapistAndDate(physioId, date)).thenReturn(slots);

        mockMvc.perform(get("/slots/{physioId}", physioId).queryParam("date", "2026-08-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(appointmentSlotService).getAvailableSlotsByTherapistAndDate(physioId, date);
    }

    @Test
    @DisplayName("Slot availability endpoint returns 200 for valid slotId")
    void checkSlotAvailability_ShouldReturnOk_WhenSlotIdIsValid() throws Exception {
        Long slotId = 1L;
        SlotAvailabilityResponse slotAvailabilityResponse = new SlotAvailabilityResponse(true);

        when(appointmentSlotService.checkAvailability(slotId)).thenReturn(slotAvailabilityResponse);

        mockMvc.perform(get("/slots/availability/{slotId}", slotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(appointmentSlotService).checkAvailability(slotId);
    }

    @Test
    @DisplayName("Find slot endpoint returns 200 for valid slotId")
    void getSlotBySlotId_ShouldReturnOk_WhenSlotIdIsValid() throws Exception {
        Long slotId = 2L;
        AppointmentSlotDTO appointmentSlotDTO = new AppointmentSlotDTO(slotId, LocalDateTime.of(2026, 8, 30, 10, 0), false, 1L);

        when(appointmentSlotService.getBySlotId(slotId)).thenReturn(appointmentSlotDTO);

        mockMvc.perform(get("/slots/findSlot/{slotId}", slotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));

        verify(appointmentSlotService).getBySlotId(slotId);
    }

    @Test
    @DisplayName("Insert endpoint returns 400 for empty list payload")
    void insertNewSlots_ShouldReturnBadRequest_WhenPayloadListIsEmpty() throws Exception {
        mockMvc.perform(post("/slots/insert")
                        .contentType(APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(appointmentSlotService, appointmentSlotMapper);
    }

    @Test
    @DisplayName("Insert endpoint returns 400 for invalid list element payload")
    void insertNewSlots_ShouldReturnBadRequest_WhenListElementIsInvalid() throws Exception {
        String invalidPayload = "[{\"startTime\":\"2020-01-01T10:00:00\",\"physioId\":0}]";

        mockMvc.perform(post("/slots/insert")
                        .contentType(APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(appointmentSlotService, appointmentSlotMapper);
    }

    @Test
    @DisplayName("Insert endpoint returns 204 and calls service for valid payload")
    void insertNewSlots_ShouldReturnNoContent_WhenPayloadIsValid() throws Exception {
        String validPayload = "[{\"startTime\":\"2099-01-01T10:00:00\",\"physioId\":1}]";
        AppointmentSlotDTO appointmentSlotDTO = new AppointmentSlotDTO(null, LocalDateTime.of(2099, 1, 1, 10, 0), false, 1L);

        when(appointmentSlotMapper.toSlotDto(any(AppointmentSlotInsertRequest.class))).thenReturn(appointmentSlotDTO);

        mockMvc.perform(post("/slots/insert")
                        .contentType(APPLICATION_JSON)
                        .content(validPayload))
                .andExpect(status().isNoContent());

        verify(appointmentSlotMapper).toSlotDto(any(AppointmentSlotInsertRequest.class));
        verify(appointmentSlotService).insertSlots(List.of(appointmentSlotDTO));
    }
}

