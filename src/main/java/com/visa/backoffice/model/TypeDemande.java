package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "type_demande")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String libelle;

    @OneToMany(mappedBy = "typeDemande")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Demande> demandes = new HashSet<>();
}
