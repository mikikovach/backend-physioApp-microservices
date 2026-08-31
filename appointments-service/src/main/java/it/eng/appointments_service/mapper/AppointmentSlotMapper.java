package it.eng.appointments_service.mapper;


import it.eng.appointments_service.dto.AppointmentSlotDTO;
import it.eng.appointments_service.dto.AppointmentSlotInsertRequest;
import it.eng.appointments_service.entity.AppointmentSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentSlotMapper {


    AppointmentSlotDTO toDto(AppointmentSlot appointmentSlot);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reserved", constant = "false")
    AppointmentSlotDTO toSlotDto(AppointmentSlotInsertRequest appointmentSlotInsertRequest);

    AppointmentSlot toEntity(AppointmentSlotDTO appointmentSlotDTO);
    List<AppointmentSlotDTO> toDtoList(List<AppointmentSlot> appointmentSlots);
    List<AppointmentSlot> toEntityList(List<AppointmentSlotDTO> appointmentSlotDTOs);
}
