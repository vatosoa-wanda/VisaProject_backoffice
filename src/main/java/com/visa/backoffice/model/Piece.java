package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "piece")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Piece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String nom;

    @Column(nullable = false)
    private Boolean obligatoire = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_type_piece", nullable = false)
    private TypePiece typePiece;

    @OneToMany(mappedBy = "piece")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<DemandePiece> demandePieces = new HashSet<>();
}
