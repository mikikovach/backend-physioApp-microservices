package it.eng.auth_service.repository;

import it.eng.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirstName(String username);
    Optional<User> findByEmail(String email);
}
