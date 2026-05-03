package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "visa_passeport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaPasseport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_visa", nullable = false)
    private Visa visa;

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
