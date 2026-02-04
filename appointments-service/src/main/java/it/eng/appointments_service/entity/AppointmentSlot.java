package it.eng.appointments_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@ToString
@Table(name = "appointment_slots")
public class AppointmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointment_slot_seq")
    @SequenceGenerator(name = "appointment_slot_seq", sequenceName = "appointment_slots_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "reserved", nullable = false)
    private boolean reserved;

    @Column(name = "physio_id", nullable = false)
    private Long physioId;
}
