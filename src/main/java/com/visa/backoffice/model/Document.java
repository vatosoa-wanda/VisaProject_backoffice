package com.visa.backoffice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_piece", nullable = false)
    private Piece piece;

    @Column(name = "nom_original", length = 255, nullable = false)
    private String nomOriginal;

    @Column(name = "chemin_fichier", length = 500, nullable = false)
    private String cheminFichier;

    @Column(name = "taille_fichier")
    private Long tailleFichier;

    @Column(name = "type_mime", length = 100)
    private String typeMime;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
}
