package com.visa.backoffice.repository;

import com.visa.backoffice.model.Personne;
import com.visa.backoffice.model.VisaActuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisaActuelRepository extends JpaRepository<VisaActuel, Integer> {
    Optional<VisaActuel> findByNumeroVisa(String numeroVisa);
    List<VisaActuel> findByPersonne(Personne personne);
    List<VisaActuel> findByPersonneId(Integer personneId);
    List<VisaActuel> findByDateExpirationBefore(LocalDate date);
    List<VisaActuel> findByDateExpirationAfter(LocalDate date);
}
