package com.visa.backoffice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "demande_piece")
public class DemandePiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande")
    private DemandeVisa demande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_piece")
    private Piece piece;

    @Column
    private Boolean fourni;

    public DemandePiece() {
        this.fourni = false;
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

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Boolean getFourni() {
        return fourni;
    }

    public void setFourni(Boolean fourni) {
        this.fourni = fourni;
    }
}
