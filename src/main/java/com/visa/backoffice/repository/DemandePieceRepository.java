package com.visa.backoffice.repository;

import com.visa.backoffice.model.DemandePiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandePieceRepository extends JpaRepository<DemandePiece, Long> {
    long countByDemandeId(Long demandeId);
}
