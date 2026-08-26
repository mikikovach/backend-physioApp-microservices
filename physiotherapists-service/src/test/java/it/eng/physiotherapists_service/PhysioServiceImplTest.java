package it.eng.physiotherapists_service;

import it.eng.physiotherapists_service.dto.PhysiotherapistDTO;
import it.eng.physiotherapists_service.entity.Physiotherapist;
import it.eng.physiotherapists_service.exception.PhysioNotFoundException;
import it.eng.physiotherapists_service.exception.PhysioServiceUnavailableException;
import it.eng.physiotherapists_service.mapper.PhysioMapper;
import it.eng.physiotherapists_service.repository.PhysioRepository;
import it.eng.physiotherapists_service.service.impl.PhysioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysioServiceImplTest {

    @Mock
    private PhysioRepository physioRepository;

    @Mock
    private PhysioMapper physioMapper;

    @InjectMocks
    private PhysioServiceImpl physioService;

    @Test
    void getAllPhysios_ShouldReturnListOfPhysiotherapistDTOs_WhenRepositoryReturnsData() {
        List<Physiotherapist> physios = List.of(new Physiotherapist(), new Physiotherapist());
        List<PhysiotherapistDTO> physioDTOs = List.of(new PhysiotherapistDTO(1L, "John", "Doe", "massage"), new PhysiotherapistDTO(2L, "Jane", "Smith", "rehabilitation"));

        when(physioRepository.findAll()).thenReturn(physios);
        when(physioMapper.toDto(any(Physiotherapist.class))).thenReturn(physioDTOs.get(0), physioDTOs.get(1));

        List<PhysiotherapistDTO> result = physioService.getAllPhysios();

        assertEquals(physioDTOs.size(), result.size());
        verify(physioRepository, times(1)).findAll();
        verify(physioMapper, times(2)).toDto(any(Physiotherapist.class));
    }

    @Test
    void getAllPhysios_ShouldThrowPhysioServiceUnavailableException_WhenFallbackIsTriggered() {
        when(physioRepository.findAll()).thenThrow(new PhysioServiceUnavailableException("Service unavailable"));

        assertThrows(PhysioServiceUnavailableException.class, () -> physioService.getAllPhysios());
    }

    @Test
    void getPhysioById_ShouldReturnPhysiotherapistDTO_WhenIdExists() {
        Long physioId = 1L;
        Physiotherapist physio = new Physiotherapist();
        PhysiotherapistDTO physioDTO = new PhysiotherapistDTO(1L, "John", "Doe", "massage");

        when(physioRepository.findById(physioId)).thenReturn(Optional.of(physio));
        when(physioMapper.toDto(physio)).thenReturn(physioDTO);

        PhysiotherapistDTO result = physioService.getPhysioById(physioId);

        assertEquals(physioDTO, result);
        verify(physioRepository, times(1)).findById(physioId);
        verify(physioMapper, times(1)).toDto(physio);
    }

    @Test
    void getPhysioById_ShouldThrowPhysioNotFoundException_WhenIdDoesNotExist() {
        Long physioId = 1L;

        when(physioRepository.findById(physioId)).thenReturn(Optional.empty());

        assertThrows(PhysioNotFoundException.class, () -> physioService.getPhysioById(physioId));
        verify(physioRepository, times(1)).findById(physioId);
    }
}