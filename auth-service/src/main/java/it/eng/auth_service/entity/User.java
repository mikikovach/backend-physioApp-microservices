package it.eng.auth_service.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id()
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "users_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long userId;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email")
    private String email;
    @Column(name="password")
    private String password;
    @Column(name="date_of_birth")
    private LocalDate birthDate;
    private String city;
    private String street;
    @Column(name = "postal_code")
    private Long postalCode;




//    @Override
//    public @Nullable String getPassword() {
//        return null;
//    }


}
