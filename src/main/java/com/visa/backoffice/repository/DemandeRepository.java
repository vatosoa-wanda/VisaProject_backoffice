package com.visa.backoffice.repository;

import com.visa.backoffice.model.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByIdDemandeOrigineAndTypeDemandeLibelle(Long idDemandeOrigine, String typeDemandeLibelle);
    List<Demande> findByStatutDemandeLibelleAndTypeDemandeLibelle(String statutLibelle, String typeLibelle);
    Optional<Demande> findFirstByIdDemandeOrigineAndTypeDemandeLibelleAndStatutDemandeLibelle(
        Long idDemandeOrigine, String typeDemandeLibelle, String statutDemandeLibelle);
}
