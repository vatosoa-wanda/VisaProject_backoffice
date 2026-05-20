package com.visa.backoffice.repository;

import com.visa.backoffice.model.VisaPasseport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisaPasseportRepository extends JpaRepository<VisaPasseport, Long> {
    /**
     * Find all visa_passeport associations for a specific visa
     */
    List<VisaPasseport> findByVisaId(Long idVisa);

    /**
     * Find all visa_passeport associations for a specific passeport
     */
    List<VisaPasseport> findByPasseportId(Long idPasseport);

    /**
     * Find all visa_passeport associations for a specific demande
     */
    List<VisaPasseport> findByDemandeId(Long idDemande);

    /**
     * Find a specific visa_passeport association by visa, passeport and demande
     */
    VisaPasseport findByVisaIdAndPasseportIdAndDemandeId(Long idVisa, Long idPasseport, Long idDemande);

    /**
     * Delete all associations for a specific visa
     */
    void deleteByVisaId(Long idVisa);

    /**
     * Delete all associations for a specific demande
     */
    void deleteByDemandeId(Long idDemande);
}
