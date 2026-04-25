package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "demandeur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Demandeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(name = "nom_jeune_fille", length = 100)
    private String nomJeuneFille;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 150)
    private String lieuNaissance;

    @ManyToOne
    @JoinColumn(name = "id_situation_familiale")
    private SituationFamiliale situationFamiliale;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_nationalite", nullable = false)
    private Nationalite nationalite;

    @Column(name = "adresse_madagascar", columnDefinition = "TEXT", nullable = false)
    private String adresseMadagascar;

    @Column(length = 20, nullable = false)
    private String telephone;

    @Column(length = 150)
    private String email;

    @OneToMany(mappedBy = "demandeur")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Passeport> passeports = new HashSet<>();

    @OneToMany(mappedBy = "demandeur")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<VisaTransformable> visasTransformables = new HashSet<>();

    @OneToMany(mappedBy = "demandeur")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Demande> demandes = new HashSet<>();
}
