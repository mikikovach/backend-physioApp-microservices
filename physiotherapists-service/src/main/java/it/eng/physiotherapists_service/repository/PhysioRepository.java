package it.eng.physiotherapists_service.repository;


import it.eng.physiotherapists_service.entity.Physiotherapist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhysioRepository extends JpaRepository<Physiotherapist, Long> {
}
