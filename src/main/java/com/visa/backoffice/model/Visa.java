package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "visa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_visa", length = 100)
    private String referenceVisa;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @OneToMany(mappedBy = "visa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VisaPasseport> visaPasseports;
}
