package com.visa.backoffice.repository;

import com.visa.backoffice.model.Demandeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DemandeurRepository extends JpaRepository<Demandeur, Long> {
    Optional<Demandeur> findByNomAndDateNaissance(String nom, LocalDate dateNaissance);
}
