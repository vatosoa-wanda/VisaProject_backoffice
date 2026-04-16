package com.visa.backoffice.repository;

import com.visa.backoffice.model.DemandeVisa;
import com.visa.backoffice.model.Personne;
import com.visa.backoffice.model.Statut;
import com.visa.backoffice.model.TypeVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemandeVisaRepository extends JpaRepository<DemandeVisa, Integer> {
    List<DemandeVisa> findByPersonne(Personne personne);
    List<DemandeVisa> findByPersonneId(Integer personneId);
    List<DemandeVisa> findByStatut(String statut);
    List<DemandeVisa> findByStatutObj(Statut statut);
    List<DemandeVisa> findByTypeVisa(TypeVisa typeVisa);
    List<DemandeVisa> findByTypeDemande(String typeDemande);
    List<DemandeVisa> findByDateDemandeAfter(LocalDateTime date);
    List<DemandeVisa> findByDateDemandeBefore(LocalDateTime date);
    List<DemandeVisa> findByDemandeOriginale(DemandeVisa demandeOriginale);
    List<DemandeVisa> findByStatutAndPersonneId(String statut, Integer personneId);
}
