package it.eng.physiotherapists_service.mapper;


import it.eng.physiotherapists_service.dto.PhysiotherapistDTO;
import it.eng.physiotherapists_service.entity.Physiotherapist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhysioMapper {
    @Mapping(source="id", target="physioId")
    PhysiotherapistDTO toDto(Physiotherapist physiotherapist);
    @Mapping(source="physioId", target="id")
    Physiotherapist toEntity(PhysiotherapistDTO physiotherapistDTO);
    List<PhysiotherapistDTO> toDtoList(List<Physiotherapist> physiotherapists);
    List<Physiotherapist> toEntityList(List<PhysiotherapistDTO> physiotherapistDTOs);
}
