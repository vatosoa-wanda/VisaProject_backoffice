package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "situation_familiale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SituationFamiliale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String libelle;

    @OneToMany(mappedBy = "situationFamiliale")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Demandeur> demandeurs = new HashSet<>();
}
