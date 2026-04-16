package com.visa.backoffice.repository;

import com.visa.backoffice.model.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatutRepository extends JpaRepository<Statut, Integer> {
    Optional<Statut> findByCode(String code);
    Optional<Statut> findByLibelle(String libelle);
}
