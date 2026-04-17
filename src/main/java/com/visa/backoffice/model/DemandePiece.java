package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "demande_piece")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandePiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_piece", nullable = false)
    private Piece piece;

    @Column(nullable = false)
    private Boolean fourni = false;
}
