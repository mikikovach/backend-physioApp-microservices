package it.eng.reservations_service.service;

import it.eng.reservations_service.config.PhysioServiceClient;
import it.eng.reservations_service.config.SlotServiceClient;
import it.eng.reservations_service.dto.PhysioDto;
import it.eng.reservations_service.dto.ReservationViewDTO;
import it.eng.reservations_service.dto.SlotDto;
import it.eng.reservations_service.entity.Reservation;
import it.eng.reservations_service.exception.ReservationNotFoundException;
import it.eng.reservations_service.exception.SlotAlreadyReservedInReservationContextException;
import it.eng.reservations_service.repository.ReservationsRepository;
import it.eng.reservations_service.service.impl.ReservationsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReservationsServiceImpl} validating service behaviour
 * at the boundary where validated inputs drive domain logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationsServiceImpl tests")
class ReservationsServiceImplTest {

    private static final Long USER_ID = 7L;
    private static final Long SLOT_ID = 42L;
    private static final Long RESERVATION_ID = 1L;
    private static final Long PHYSIO_ID = 10L;

    @Mock
    private ReservationsRepository reservationsRepository;

    @Mock
    private SlotServiceClient slotServiceClient;

    @Mock
    private PhysioServiceClient physioServiceClient;

    @InjectMocks
    private ReservationsServiceImpl reservationsService;

    // ------------------------------------------------------------------ createReservation

    @Test
    @DisplayName("createReservation saves entity and returns it when slot is reserved successfully")
    void createReservation_ShouldSaveReservation_WhenSlotReservedSuccessfully() {
        Reservation saved = buildReservation(RESERVATION_ID, USER_ID, SLOT_ID);
        when(slotServiceClient.reserveSlotRemotely(SLOT_ID)).thenReturn(Mono.empty());
        when(reservationsRepository.save(any(Reservation.class))).thenReturn(saved);

        Reservation result = reservationsService.createReservation(USER_ID, SLOT_ID).block();

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getSlotId()).isEqualTo(SLOT_ID);
        verify(reservationsRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("createReservation propagates SlotAlreadyReservedException without releasing the slot")
    void createReservation_ShouldPropagateSlotAlreadyReservedException_WithoutRelease() {
        SlotAlreadyReservedInReservationContextException conflict =
                new SlotAlreadyReservedInReservationContextException("Slot already reserved");
        when(slotServiceClient.reserveSlotRemotely(SLOT_ID)).thenReturn(Mono.error(conflict));

        assertThatThrownBy(() -> reservationsService.createReservation(USER_ID, SLOT_ID).block())
                .isInstanceOf(SlotAlreadyReservedInReservationContextException.class);

        verify(slotServiceClient, never()).releaseSlot(any());
        verify(reservationsRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReservation releases slot when a non-conflict error occurs after reservation attempt")
    void createReservation_ShouldReleaseSlot_OnOtherError() {
        RuntimeException serviceError = new RuntimeException("Unexpected error");
        when(slotServiceClient.reserveSlotRemotely(SLOT_ID)).thenReturn(Mono.error(serviceError));
        when(slotServiceClient.releaseSlot(SLOT_ID)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> reservationsService.createReservation(USER_ID, SLOT_ID).block())
                .hasMessage("Unexpected error");

        verify(slotServiceClient).releaseSlot(SLOT_ID);
    }

    // ------------------------------------------------------------------ getMyReservations

    @Test
    @DisplayName("getMyReservations returns mapped view DTOs for the authenticated user")
    void getMyReservations_ShouldReturnMappedViewDTOs() {
        Reservation reservation = buildReservation(RESERVATION_ID, USER_ID, SLOT_ID);
        SlotDto slotDto = new SlotDto(SLOT_ID, PHYSIO_ID, LocalDateTime.of(2026, 9, 1, 10, 0));
        PhysioDto physioDto = new PhysioDto(PHYSIO_ID, "Anna", "Novak");

        when(reservationsRepository.findByUserId(USER_ID)).thenReturn(List.of(reservation));
        when(slotServiceClient.fetchSlotBySlotId(SLOT_ID)).thenReturn(slotDto);
        when(physioServiceClient.fetchPhysioByPhysioId(PHYSIO_ID)).thenReturn(physioDto);

        List<ReservationViewDTO> result = reservationsService.getMyReservations(USER_ID);

        assertThat(result).hasSize(1);
        ReservationViewDTO view = result.getFirst();
        assertThat(view.userId()).isEqualTo(USER_ID);
        assertThat(view.slotId()).isEqualTo(SLOT_ID);
        assertThat(view.therapistName()).isEqualTo("Anna");
        assertThat(view.therapistSurname()).isEqualTo("Novak");
        assertThat(view.startTime()).isEqualTo(slotDto.startTime());
    }

    @Test
    @DisplayName("getMyReservations returns empty list when user has no reservations")
    void getMyReservations_ShouldReturnEmptyList_WhenNoReservationsExist() {
        when(reservationsRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<ReservationViewDTO> result = reservationsService.getMyReservations(USER_ID);

        assertThat(result).isEmpty();
        verify(slotServiceClient, never()).fetchSlotBySlotId(any());
        verify(physioServiceClient, never()).fetchPhysioByPhysioId(any());
    }

    // ------------------------------------------------------------------ cancelReservation

    @Test
    @DisplayName("cancelReservation deletes reservation and releases slot when slot ID is set")
    void cancelReservation_ShouldDeleteAndReleaseSlot_WhenReservationExists() {
        Reservation reservation = buildReservation(RESERVATION_ID, USER_ID, SLOT_ID);
        when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));
        when(slotServiceClient.releaseSlot(SLOT_ID)).thenReturn(Mono.empty());

        reservationsService.cancelReservation(RESERVATION_ID);

        verify(reservationsRepository).delete(reservation);
        verify(slotServiceClient).releaseSlot(SLOT_ID);
    }

    @Test
    @DisplayName("cancelReservation deletes reservation without calling slot release when slot ID is null")
    void cancelReservation_ShouldDeleteWithoutRelease_WhenSlotIdIsNull() {
        Reservation reservation = buildReservation(RESERVATION_ID, USER_ID, null);
        when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

        reservationsService.cancelReservation(RESERVATION_ID);

        verify(reservationsRepository).delete(reservation);
        verify(slotServiceClient, never()).releaseSlot(any());
    }

    @Test
    @DisplayName("cancelReservation throws ReservationNotFoundException when reservation does not exist")
    void cancelReservation_ShouldThrowReservationNotFoundException_WhenNotFound() {
        when(reservationsRepository.findById(RESERVATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationsService.cancelReservation(RESERVATION_ID))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining(String.valueOf(RESERVATION_ID));

        verify(reservationsRepository, never()).delete(any());
        verify(slotServiceClient, never()).releaseSlot(any());
    }

    // ------------------------------------------------------------------ helpers

    private Reservation buildReservation(Long id, Long userId, Long slotId) {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setUserId(userId);
        reservation.setSlotId(slotId);
        reservation.setCreatedAt(LocalDateTime.of(2026, 8, 31, 9, 0));
        return reservation;
    }
}



