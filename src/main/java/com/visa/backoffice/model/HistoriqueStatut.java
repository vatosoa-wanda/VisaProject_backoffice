package com.visa.backoffice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande")
    private DemandeVisa demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_statut")
    private Statut statut;

    @Column(name = "date_changement")
    private LocalDateTime dateChangement;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    public HistoriqueStatut() {
        this.dateChangement = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DemandeVisa getDemande() {
        return demande;
    }

    public void setDemande(DemandeVisa demande) {
        this.demande = demande;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateChangement() {
        return dateChangement;
    }

    public void setDateChangement(LocalDateTime dateChangement) {
        this.dateChangement = dateChangement;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
