package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "visa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_visa", length = 100)
    private String referenceVisa;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @ManyToOne
    @JoinColumn(name = "id_passeport")
    private Passeport passeport;

    @ManyToOne
    @JoinColumn(name = "id_demande")
    private Demande demande;
}
