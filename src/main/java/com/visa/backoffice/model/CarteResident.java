package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "carte_resident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteResident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_carte", length = 50, unique = true)
    private String numeroCarte;

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
