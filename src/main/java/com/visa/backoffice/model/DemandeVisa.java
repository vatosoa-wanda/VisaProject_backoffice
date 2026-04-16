package com.visa.backoffice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "demande_visa")
public class DemandeVisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @Column(name = "type_demande", length = 50)
    private String typeDemande; // NOUVELLE / RENOUVELLEMENT

    @Column(length = 50)
    private String statut; // EN_ATTENTE / EN_COURS / VALIDEE / REFUSEE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_personne")
    private Personne personne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_passeport")
    private Passeport passeport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_visa_actuel")
    private VisaActuel visaActuel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_visa")
    private TypeVisa typeVisa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_statut")
    private Statut statutObj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demande_originale")
    private DemandeVisa demandeOriginale;

    @OneToMany(mappedBy = "demandeOriginale", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandeVisa> demandesRelated = new HashSet<>();

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HistoriqueStatut> historiques = new HashSet<>();

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandePiece> demandePieces = new HashSet<>();

    public DemandeVisa() {
        this.dateDemande = LocalDateTime.now();
        this.typeDemande = "NOUVELLE";
        this.statut = "EN_ATTENTE";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public String getTypeDemande() {
        return typeDemande;
    }

    public void setTypeDemande(String typeDemande) {
        this.typeDemande = typeDemande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    public Passeport getPasseport() {
        return passeport;
    }

    public void setPasseport(Passeport passeport) {
        this.passeport = passeport;
    }

    public VisaActuel getVisaActuel() {
        return visaActuel;
    }

    public void setVisaActuel(VisaActuel visaActuel) {
        this.visaActuel = visaActuel;
    }

    public TypeVisa getTypeVisa() {
        return typeVisa;
    }

    public void setTypeVisa(TypeVisa typeVisa) {
        this.typeVisa = typeVisa;
    }

    public Statut getStatutObj() {
        return statutObj;
    }

    public void setStatutObj(Statut statutObj) {
        this.statutObj = statutObj;
    }

    public DemandeVisa getDemandeOriginale() {
        return demandeOriginale;
    }

    public void setDemandeOriginale(DemandeVisa demandeOriginale) {
        this.demandeOriginale = demandeOriginale;
    }

    public Set<DemandeVisa> getDemandesRelated() {
        return demandesRelated;
    }

    public void setDemandesRelated(Set<DemandeVisa> demandesRelated) {
        this.demandesRelated = demandesRelated;
    }

    public Set<HistoriqueStatut> getHistoriques() {
        return historiques;
    }

    public void setHistoriques(Set<HistoriqueStatut> historiques) {
        this.historiques = historiques;
    }

    public Set<DemandePiece> getDemandePieces() {
        return demandePieces;
    }

    public void setDemandePieces(Set<DemandePiece> demandePieces) {
        this.demandePieces = demandePieces;
    }
}
