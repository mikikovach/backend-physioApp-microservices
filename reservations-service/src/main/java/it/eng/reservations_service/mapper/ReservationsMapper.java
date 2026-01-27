package it.eng.reservations_service.mapper;

import it.eng.reservations_service.dto.ReservationDTO;
import it.eng.reservations_service.entity.Reservation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationsMapper {

    public ReservationDTO entitytoDto(Reservation reservation);
}
