package com.visa.backoffice.repository;

import com.visa.backoffice.model.TypeDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypeDemandeRepository extends JpaRepository<TypeDemande, Long> {
    Optional<TypeDemande> findByLibelle(String libelle);
}
