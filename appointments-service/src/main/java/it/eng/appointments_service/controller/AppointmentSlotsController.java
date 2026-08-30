package it.eng.appointments_service.controller;


import it.eng.appointments_service.dto.AppointmentSlotDTO;
import it.eng.appointments_service.dto.SlotAvailabilityResponse;
import it.eng.appointments_service.service.AppointmentSlotService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/slots")
@AllArgsConstructor
@Validated
public class AppointmentSlotsController {


    private final AppointmentSlotService appointmentSlotService;

    @GetMapping("/{physioId}")
    public ResponseEntity<List<AppointmentSlotDTO>> getAvailableSlotsByPhysio(@PathVariable @NotNull @Positive Long physioId,
                                                                               @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentSlotDTO> availableSlots = appointmentSlotService.getAvailableSlotsByTherapistAndDate(physioId, date);
        return new ResponseEntity<>(availableSlots, HttpStatus.OK);
    }
    @GetMapping("/admin/{physioId}")
    public ResponseEntity<List<AppointmentSlotDTO>> getAvailableSlotsForAdminByPhysio(@PathVariable @NotNull @Positive Long physioId,
                                                                                       @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentSlotDTO> availableSlots = appointmentSlotService.getAvailableSlotsForAdminByTherapistAndDate(physioId, date);
        return new ResponseEntity<>(availableSlots, HttpStatus.OK);
    }

    @GetMapping("/availability/{slotId}")
    public SlotAvailabilityResponse checkSlotAvailability(@PathVariable @NotNull @Positive Long slotId) {
        return appointmentSlotService.checkAvailability(slotId);

    }

    @PostMapping("/reserve/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserveSlot(@PathVariable @NotNull @Positive Long slotId) {
         appointmentSlotService.reserveSlot(slotId);

    }

    @PostMapping("/release/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseSlot(@PathVariable @NotNull @Positive Long slotId) {
        appointmentSlotService.releaseSlot(slotId);

    }

    @GetMapping("/findSlot/{slotId}")
    public AppointmentSlotDTO getSlotBySlotId(@PathVariable @NotNull @Positive Long slotId) {
       return appointmentSlotService.getBySlotId(slotId);

    }

    @PostMapping("/insert")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void insertNewSlots(@RequestBody @NotEmpty List<@Valid AppointmentSlotDTO> slotDTOList) {
        appointmentSlotService.insertSlots(slotDTOList);

    }

}
