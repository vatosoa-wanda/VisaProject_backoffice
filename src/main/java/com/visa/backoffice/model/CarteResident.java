package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "carte_resident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteResident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_carte", length = 50, unique = true)
    private String numeroCarte;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @OneToMany(mappedBy = "carteResident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarteResidentPasseport> carteResidentPasseports;
}
