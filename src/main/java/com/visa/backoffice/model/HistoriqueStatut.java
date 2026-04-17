package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_statut")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_statut_demande", nullable = false)
    private StatutDemande statutDemande;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @Column(columnDefinition = "TEXT")
    private String commentaire;
}
