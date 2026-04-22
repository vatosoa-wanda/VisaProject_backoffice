package com.visa.backoffice.repository;

import com.visa.backoffice.model.DemandePiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandePieceRepository extends JpaRepository<DemandePiece, Long> {
    List<DemandePiece> findByDemandeId(Long demandeId);

    void deleteByDemandeId(Long demandeId);
}
