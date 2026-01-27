package it.eng.appointments_service.repository;


import it.eng.appointments_service.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByPhysioIdAndReservedFalseAndStartTimeBetween(Long therapistId, LocalDateTime dayStart, LocalDateTime dayEnd);
}
