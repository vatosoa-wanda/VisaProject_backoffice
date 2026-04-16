package com.visa.backoffice.repository;

import com.visa.backoffice.model.Personne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonneRepository extends JpaRepository<Personne, Integer> {
    Optional<Personne> findByEmail(String email);
    Optional<Personne> findByTelephone(String telephone);
    List<Personne> findByNom(String nom);
    List<Personne> findByPrenom(String prenom);
    List<Personne> findByNomAndPrenom(String nom, String prenom);
    List<Personne> findByNationalite(String nationalite);
}
