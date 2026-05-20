package com.visa.backoffice.repository;

import com.visa.backoffice.model.CarteResidentPasseport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarteResidentPasseportRepository extends JpaRepository<CarteResidentPasseport, Long> {
    /**
     * Find all carte_resident_passeport associations for a specific carte resident
     */
    List<CarteResidentPasseport> findByCarteResidentId(Long idCarteResident);

    /**
     * Find all carte_resident_passeport associations for a specific passeport
     */
    List<CarteResidentPasseport> findByPasseportId(Long idPasseport);

    /**
     * Find all carte_resident_passeport associations for a specific demande
     */
    List<CarteResidentPasseport> findByDemandeId(Long idDemande);

    /**
     * Find a specific carte_resident_passeport association by carte resident, passeport and demande
     */
    CarteResidentPasseport findByCarteResidentIdAndPasseportIdAndDemandeId(Long idCarteResident, Long idPasseport, Long idDemande);

    /**
     * Delete all associations for a specific carte resident
     */
    void deleteByCarteResidentId(Long idCarteResident);

    /**
     * Delete all associations for a specific demande
     */
    void deleteByDemandeId(Long idDemande);
}
