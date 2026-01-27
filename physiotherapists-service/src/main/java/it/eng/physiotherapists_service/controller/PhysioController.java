package it.eng.physiotherapists_service.controller;


import it.eng.physiotherapists_service.dto.PhysiotherapistDTO;
import it.eng.physiotherapists_service.service.PhysioService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/physios")
@AllArgsConstructor
@Slf4j
public class PhysioController {

    private final PhysioService physioService;

    @GetMapping
    public ResponseEntity<List<PhysiotherapistDTO>> getAllPhysios() {
        List<PhysiotherapistDTO> physios = physioService.getAllPhysios();

        log.info("Retrieved {} physiotherapists", physios.size());
        return new ResponseEntity<>(physios, HttpStatus.OK);
    }

    @GetMapping("/{physioId}")
    public PhysiotherapistDTO getPhysioById(@PathVariable Long physioId) {

        return physioService.getPhysioById(physioId);

    }
}
