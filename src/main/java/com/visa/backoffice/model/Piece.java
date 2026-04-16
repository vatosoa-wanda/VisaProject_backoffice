package com.visa.backoffice.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "piece")
public class Piece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255)
    private String nom;

    @Column(length = 50)
    private String type; // COMMUN / INVESTISSEUR / TRAVAILLEUR

    @OneToMany(mappedBy = "piece", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandePiece> demandePieces = new HashSet<>();

    public Piece() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<DemandePiece> getDemandePieces() {
        return demandePieces;
    }

    public void setDemandePieces(Set<DemandePiece> demandePieces) {
        this.demandePieces = demandePieces;
    }
}
