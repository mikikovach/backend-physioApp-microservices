package it.eng.appointments_service.service.impl;


import it.eng.appointments_service.dto.AppointmentSlotDTO;
import it.eng.appointments_service.dto.SlotAvailabilityResponse;
import it.eng.appointments_service.entity.AppointmentSlot;
import it.eng.appointments_service.exception.SlotAlreadyBookedException;
import it.eng.appointments_service.exception.SlotInPastException;
import it.eng.appointments_service.exception.SlotNotBookedException;
import it.eng.appointments_service.exception.SlotNotFoundException;
import it.eng.appointments_service.mapper.AppointmentSlotMapper;
import it.eng.appointments_service.repository.AppointmentSlotRepository;
import it.eng.appointments_service.service.AppointmentSlotService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final AppointmentSlotMapper appointmentSlotMapper;

    private static final String SLOT_NOT_FOUND_MESSAGE = "Slot not found with id: ";


    @Override
    public List<AppointmentSlotDTO> getAvailableSlotsByTherapistAndDate(Long physiotherapistId, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentSlotRepository.findByPhysioIdAndReservedFalseAndStartTimeBetween(physiotherapistId, startOfDay, endOfDay)
                .stream()
                .map(appointmentSlotMapper::toDto)
                .toList();
    }

    @Override
    public List<AppointmentSlotDTO> getAvailableSlotsForAdminByTherapistAndDate(Long physioId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return appointmentSlotRepository.findByPhysioIdAndStartTimeBetween(physioId, startOfDay, endOfDay)
                .stream()
                .map(appointmentSlotMapper::toDto)
                .toList();
    }

    @Override
    public List<AppointmentSlot> insertSlots(List<AppointmentSlotDTO> slotDTOList) {
        log.info("Inserting slots...");
        List<AppointmentSlot> slots = slotDTOList.stream()
                .map(appointmentSlotMapper::toEntity)
                .toList();
       return appointmentSlotRepository.saveAll(slots);
    }

    @Override
    public SlotAvailabilityResponse checkAvailability(Long slotId) {
        log.info("Checking availability  from appointments controller for slot id: {}", slotId);

        AppointmentSlot slot = appointmentSlotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(SLOT_NOT_FOUND_MESSAGE + slotId));

        log.info("Slot found: {}", slot.toString());

        if (slot.isReserved()) {
            throw new SlotAlreadyBookedException("Slot already booked");
        }
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new SlotInPastException("Slot in the past");
        }

        return new SlotAvailabilityResponse(true);
    }

    @Override
    public AppointmentSlot getReservableSlot(Long slotId) {

        AppointmentSlot slot = appointmentSlotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(SLOT_NOT_FOUND_MESSAGE + slotId));

        log.info("Slot found: {}", slot);

        if (slot.isReserved()) {
            throw new SlotAlreadyBookedException("Slot already booked");
        }
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new SlotInPastException("Slot in the past");
        }

        return slot;
    }

    @Override
    public AppointmentSlot getReleasableSlot(Long slotId) {

        AppointmentSlot slot = appointmentSlotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(SLOT_NOT_FOUND_MESSAGE + slotId));

        log.info("Found slot for release: {}", slot);

        if (!slot.isReserved()) {
            throw new SlotNotBookedException("Slot is not booked at all");
        }
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new SlotInPastException("Slot in the past");
        }

        return slot;
    }


    @Transactional
    @Override
    public void reserveSlot(Long slotId) {

        log.info("Slots service: reserving slot with id: {}", slotId);
        AppointmentSlot slot = getReservableSlot(slotId);
        slot.setReserved(true);
        appointmentSlotRepository.save(slot);
    }

    @Transactional
    @Override
    public void releaseSlot(Long slotId) {

        AppointmentSlot slot = getReleasableSlot(slotId);
        slot.setReserved(false);
        appointmentSlotRepository.save(slot);
        log.info("Slot with id: {} released", slotId);
    }



    @Override
    public AppointmentSlotDTO getBySlotId(Long slotId) {
        return appointmentSlotRepository.findById(slotId)
                .map(appointmentSlotMapper::toDto)
                .orElseThrow(() -> new SlotNotFoundException(SLOT_NOT_FOUND_MESSAGE + slotId));
    }
}
