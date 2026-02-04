package it.eng.appointments_service.service;



import it.eng.appointments_service.dto.AppointmentSlotDTO;
import it.eng.appointments_service.dto.SlotAvailabilityResponse;
import it.eng.appointments_service.entity.AppointmentSlot;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentSlotService {

    List<AppointmentSlotDTO> getAvailableSlotsByTherapistAndDate(Long physiotherapistId, LocalDate date);

    SlotAvailabilityResponse checkAvailability(Long slotId);

    AppointmentSlot getReservableSlot(Long slotId);

    void reserveSlot(Long slotId);

    AppointmentSlotDTO getBySlotId(Long slotId);

    AppointmentSlot getReleasableSlot(Long slotId);

    void releaseSlot(Long slotId);
}
