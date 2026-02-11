package it.eng.physiotherapists_service.service.impl;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import it.eng.physiotherapists_service.dto.PhysiotherapistDTO;
import it.eng.physiotherapists_service.exception.PhysioNotFoundException;
import it.eng.physiotherapists_service.exception.PhysioServiceUnavailableException;
import it.eng.physiotherapists_service.mapper.PhysioMapper;
import it.eng.physiotherapists_service.repository.PhysioRepository;
import it.eng.physiotherapists_service.service.PhysioService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PhysioServiceImpl implements PhysioService {

    private final PhysioRepository physioRepository;

    private final PhysioMapper physioMapper;

    @Override
    @Retry(name = "physio-service")
    @CircuitBreaker(name = "physio-service", fallbackMethod = "getPhysioFallback")
    public List<PhysiotherapistDTO> getAllPhysios() {

        return physioRepository.findAll()
                .stream()
                .map(physioMapper::toDto)
                .toList();
    }

    private List<PhysiotherapistDTO> getPhysioFallback(Throwable ex) {
        throw new PhysioServiceUnavailableException("Physio service temporarily unavailable");
    }

    @Override
    public PhysiotherapistDTO getPhysioById(Long physioId) {
        return physioRepository.findById(physioId)
                .map(physioMapper::toDto)
                .orElseThrow(() -> new PhysioNotFoundException("Physiotherapist not found with id: " + physioId));
    }


}
