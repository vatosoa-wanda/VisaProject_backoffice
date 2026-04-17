package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "nationalite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nationalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String libelle;

    @OneToMany(mappedBy = "nationalite")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Demandeur> demandeurs = new HashSet<>();
}
