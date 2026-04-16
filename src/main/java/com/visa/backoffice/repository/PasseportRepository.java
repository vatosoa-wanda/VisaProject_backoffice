package com.visa.backoffice.repository;

import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasseportRepository extends JpaRepository<Passeport, Integer> {
    Optional<Passeport> findByNumero(String numero);
    List<Passeport> findByPersonne(Personne personne);
    List<Passeport> findByPersonneId(Integer personneId);
    List<Passeport> findByDateExpirationBefore(LocalDate date);
    List<Passeport> findByDateExpirationAfter(LocalDate date);
}
