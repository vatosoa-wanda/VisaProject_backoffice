package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "statut_demande")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String libelle;

    @OneToMany(mappedBy = "statutDemande")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Demande> demandes = new HashSet<>();

    @OneToMany(mappedBy = "statutDemande")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<HistoriqueStatut> historiques = new HashSet<>();
}
