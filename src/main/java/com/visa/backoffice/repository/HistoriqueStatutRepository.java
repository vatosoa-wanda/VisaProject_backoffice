package com.visa.backoffice.repository;

import com.visa.backoffice.model.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {
    List<HistoriqueStatut> findByDemandeIdOrderByDateChangementAsc(Long demandeId);
}
