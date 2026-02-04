package it.eng.appointments_service.controller;


import it.eng.appointments_service.dto.AppointmentSlotDTO;
import it.eng.appointments_service.dto.SlotAvailabilityResponse;
import it.eng.appointments_service.entity.AppointmentSlot;
import it.eng.appointments_service.service.AppointmentSlotService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/slots")
@AllArgsConstructor
public class AppointmentSlotsController {


    private final AppointmentSlotService appointmentSlotService;

    @GetMapping("/{physioId}")
    public ResponseEntity<List<AppointmentSlotDTO>> getAvailableSlotsByPhysio(@PathVariable Long physioId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentSlotDTO> availableSlots = appointmentSlotService.getAvailableSlotsByTherapistAndDate(physioId, date);
        return new ResponseEntity<>(availableSlots, HttpStatus.OK);
    }

    @GetMapping("/availability/{slotId}")
    public SlotAvailabilityResponse checkSlotAvailability(@PathVariable Long slotId) {
        return appointmentSlotService.checkAvailability(slotId);

    }

    @PostMapping("/reserve/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserveSlot(@PathVariable Long slotId) {
         appointmentSlotService.reserveSlot(slotId);

    }

    @PostMapping("/release/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseSlot(@PathVariable Long slotId) {
        appointmentSlotService.releaseSlot(slotId);

    }

    @GetMapping("/findSlot/{slotId}")
    public AppointmentSlotDTO getSlotBySlotId(@PathVariable Long slotId) {
       return appointmentSlotService.getBySlotId(slotId);

    }

}
