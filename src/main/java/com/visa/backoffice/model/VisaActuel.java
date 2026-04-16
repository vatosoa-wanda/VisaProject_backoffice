package com.visa.backoffice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "visa_actuel")
public class VisaActuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_visa", length = 50)
    private String numeroVisa;

    @Column(name = "date_entree")
    private LocalDate dateEntree;

    @Column(name = "lieu_entree", length = 100)
    private String lieuEntree;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "reference_visa", length = 100)
    private String referenceVisa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personne")
    private Personne personne;

    public VisaActuel() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumeroVisa() {
        return numeroVisa;
    }

    public void setNumeroVisa(String numeroVisa) {
        this.numeroVisa = numeroVisa;
    }

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    public void setDateEntree(LocalDate dateEntree) {
        this.dateEntree = dateEntree;
    }

    public String getLieuEntree() {
        return lieuEntree;
    }

    public void setLieuEntree(String lieuEntree) {
        this.lieuEntree = lieuEntree;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getReferenceVisa() {
        return referenceVisa;
    }

    public void setReferenceVisa(String referenceVisa) {
        this.referenceVisa = referenceVisa;
    }

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }
}
