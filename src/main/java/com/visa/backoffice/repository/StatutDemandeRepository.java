package com.visa.backoffice.repository;

import com.visa.backoffice.model.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatutDemandeRepository extends JpaRepository<StatutDemande, Long> {
    Optional<StatutDemande> findByLibelle(String libelle);
}
