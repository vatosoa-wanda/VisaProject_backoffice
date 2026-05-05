package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "carte_resident_passeport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarteResidentPasseport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_carte_resident", nullable = false)
    private CarteResident carteResident;

    @ManyToOne
    @JoinColumn(name = "id_passeport", nullable = false)
    private Passeport passeport;

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @Column(name = "date_association")
    private LocalDateTime dateAssociation;

    @PrePersist
    protected void onCreate() {
        if (dateAssociation == null) {
            dateAssociation = LocalDateTime.now();
        }
    }
}
