package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "demande")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;

    @ManyToOne
    @JoinColumn(name = "id_visa_transformable")
    private VisaTransformable visaTransformable;

    @ManyToOne
    @JoinColumn(name = "id_type_visa")
    private TypeVisa typeVisa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_type_demande", nullable = false)
    private TypeDemande typeDemande;

    @ManyToOne
    @JoinColumn(name = "id_statut_demande")
    private StatutDemande statutDemande;

    @Column(name = "id_demande_origine")
    private Long idDemandeOrigine;

    @ManyToOne
    @JoinColumn(name = "id_demande_origine", insertable = false, updatable = false)
    private Demande demandeOrigine;

    @OneToMany(mappedBy = "demande")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Visa> visas = new HashSet<>();

    @OneToMany(mappedBy = "demande")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CarteResident> cartesResidents = new HashSet<>();

    @OneToMany(mappedBy = "demande")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<HistoriqueStatut> historiques = new HashSet<>();

    @OneToMany(mappedBy = "demande")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<DemandePiece> demandePieces = new HashSet<>();
}
