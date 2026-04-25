package com.visa.backoffice.repository;

import com.visa.backoffice.model.Visa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisaRepository extends JpaRepository<Visa, Long> {
    /**
     * Find visa by demande ID
     */
    Visa findByDemandeId(Long idDemande);

    /**
     * Find visa by passeport ID
     */
    Visa findByPasseportId(Long idPasseport);

    /**
     * Find visas by demande ID and date_fin after specified date
     */
    List<Visa> findByDemandeIdAndDateFinAfter(Long idDemande, LocalDate date);
}
