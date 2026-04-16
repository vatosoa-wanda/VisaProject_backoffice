package com.visa.backoffice.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "type_visa")
public class TypeVisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20)
    private String code;

    @Column(length = 100)
    private String libelle;

    @OneToMany(mappedBy = "typeVisa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DemandeVisa> demandesVisa = new HashSet<>();

    public TypeVisa() {
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

    public Set<DemandeVisa> getDemandesVisa() {
        return demandesVisa;
    }

    public void setDemandesVisa(Set<DemandeVisa> demandesVisa) {
        this.demandesVisa = demandesVisa;
    }
}
