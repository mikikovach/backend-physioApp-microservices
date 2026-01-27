package it.eng.physiotherapists_service.service;



import it.eng.physiotherapists_service.dto.PhysiotherapistDTO;

import java.util.List;

public interface PhysioService {
    List<PhysiotherapistDTO> getAllPhysios();

    PhysiotherapistDTO getPhysioById(Long physioId);
}
