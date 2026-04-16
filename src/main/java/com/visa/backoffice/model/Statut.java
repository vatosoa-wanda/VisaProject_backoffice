package com.visa.backoffice.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "statut")
public class Statut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, unique = true)
    private String code;

    @Column(length = 100)
    private String libelle;

    @OneToMany(mappedBy = "statut", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HistoriqueStatut> historiques = new HashSet<>();

    @OneToMany(mappedBy = "statut", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandeVisa> demandesVisa = new HashSet<>();

    public Statut() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Set<HistoriqueStatut> getHistoriques() {
        return historiques;
    }

    public void setHistoriques(Set<HistoriqueStatut> historiques) {
        this.historiques = historiques;
    }

    public Set<DemandeVisa> getDemandesVisa() {
        return demandesVisa;
    }

    public void setDemandesVisa(Set<DemandeVisa> demandesVisa) {
        this.demandesVisa = demandesVisa;
    }
}
