package it.eng.api_bff_service.controller;

import it.eng.api_bff_service.dto.ReservationViewDTO;
import it.eng.api_bff_service.service.BffReservationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/views")
@AllArgsConstructor
public class BffController {

    private final BffReservationService bffReservationService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationViewDTO>> getMyReservations(@RequestHeader("X-User-Id") Long userId) {

        List<ReservationViewDTO> reservations = bffReservationService.aggregateData(userId);
        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }
}
