package com.visa.backoffice.repository;

import com.visa.backoffice.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Récupère tous les documents d'une demande
     */
    List<Document> findByDemandeId(Long demandeId);

    /**
     * Récupère un document spécifique avec vérification de propriété
     */
    Optional<Document> findByIdAndDemandeId(Long id, Long demandeId);

    /**
     * Compte les documents d'une demande
     */
    long countByDemandeId(Long demandeId);

    /**
     * Récupère un document existant pour une demande et une pièce spécifiques
     */
    Optional<Document> findByDemandeIdAndPieceId(Long demandeId, Long pieceId);
}
