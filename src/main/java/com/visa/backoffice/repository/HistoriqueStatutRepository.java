package com.visa.backoffice.repository;

import com.visa.backoffice.model.DemandeVisa;
import com.visa.backoffice.model.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Integer> {

    List<HistoriqueStatut> findByDemande(DemandeVisa demande);

    List<HistoriqueStatut> findByDemandeId(Integer demandeId);

    void deleteByDemande(DemandeVisa demande);
}