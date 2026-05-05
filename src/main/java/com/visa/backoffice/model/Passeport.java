package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "passeport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passeport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true)
    private String numero;

    @Column(name = "date_delivrance")
    private LocalDate dateDelivrance;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @ManyToOne
    @JoinColumn(name = "id_demandeur")
    private Demandeur demandeur;


    @OneToMany(mappedBy = "passeport")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CarteResidentPasseport> carteResidentPasseports = new HashSet<>();
}
