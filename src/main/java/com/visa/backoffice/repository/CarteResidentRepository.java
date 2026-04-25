package com.visa.backoffice.repository;

import com.visa.backoffice.model.CarteResident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarteResidentRepository extends JpaRepository<CarteResident, Long> {
    /**
     * Find carte resident by demande ID
     */
    CarteResident findByDemandeId(Long idDemande);

    /**
     * Find carte resident by numero_carte (unique)
     */
    CarteResident findByNumeroCarte(String numeroCarte);
}
