package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "visa_transformable")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaTransformable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_visa", length = 100)
    private String referenceVisa;

    @Column(name = "date_entree")
    private LocalDate dateEntree;

    @Column(name = "lieu_entree", length = 100)
    private String lieuEntree;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_passeport")
    private Passeport passeport;

    @OneToMany(mappedBy = "visaTransformable")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Demande> demandes = new HashSet<>();
}
