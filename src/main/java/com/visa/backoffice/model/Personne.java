package com.visa.backoffice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "personne")
public class Personne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(name = "nom_jeune_fille", length = 100)
    private String nomJeuneFille;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 150)
    private String lieuNaissance;

    @Column(name = "situation_familiale", length = 50)
    private String situationFamiliale;

    @Column(length = 100)
    private String nationalite;

    @Column(length = 100)
    private String profession;

    @Column(name = "adresse_madagascar", columnDefinition = "TEXT")
    private String adresseMadagascar;

    @Column(length = 20)
    private String telephone;

    @Column(length = 150)
    private String email;

    @OneToMany(mappedBy = "personne", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Passeport> passeports = new HashSet<>();

    @OneToMany(mappedBy = "personne", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VisaActuel> visasActuels = new HashSet<>();

    @OneToMany(mappedBy = "personne", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandeVisa> demandesVisa = new HashSet<>();

    public Personne() {
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNomJeuneFille() {
        return nomJeuneFille;
    }

    public void setNomJeuneFille(String nomJeuneFille) {
        this.nomJeuneFille = nomJeuneFille;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getSituationFamiliale() {
        return situationFamiliale;
    }

    public void setSituationFamiliale(String situationFamiliale) {
        this.situationFamiliale = situationFamiliale;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getAdresseMadagascar() {
        return adresseMadagascar;
    }

    public void setAdresseMadagascar(String adresseMadagascar) {
        this.adresseMadagascar = adresseMadagascar;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Passeport> getPasseports() {
        return passeports;
    }

    public void setPasseports(Set<Passeport> passeports) {
        this.passeports = passeports;
    }

    public Set<VisaActuel> getVisasActuels() {
        return visasActuels;
    }

    public void setVisasActuels(Set<VisaActuel> visasActuels) {
        this.visasActuels = visasActuels;
    }

    public Set<DemandeVisa> getDemandesVisa() {
        return demandesVisa;
    }

    public void setDemandesVisa(Set<DemandeVisa> demandesVisa) {
        this.demandesVisa = demandesVisa;
    }
}
