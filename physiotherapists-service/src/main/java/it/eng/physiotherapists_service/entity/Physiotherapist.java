package it.eng.physiotherapists_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "physiotherapists")
public class Physiotherapist {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "physio_seq")
    @SequenceGenerator(name = "physio_seq", sequenceName = "physio_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    @Column(name="specialization")
    private String specialization;
}