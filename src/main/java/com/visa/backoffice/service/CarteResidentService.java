package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.CarteResident;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.repository.CarteResidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CarteResidentService {

    private final CarteResidentRepository carteResidentRepository;

    public CarteResidentService(CarteResidentRepository carteResidentRepository) {
        this.carteResidentRepository = carteResidentRepository;
    }

    /**
     * Créer une nouvelle carte résident
     * RG-01 : appelé après approbation d'une demande NOUVELLE
     * RG-06 : appelé pour créer une nouvelle carte en DUPLICATA
     *
     * @param demande The demande to link with
     * @param passeport The passeport to link with
     * @return Created CarteResident with generated numeroCarte and dateDebut=today
     * @throws BusinessException if passeport is null
     */
    public CarteResident creer(Demande demande, Passeport passeport) {
        if (passeport == null) {
            throw new BusinessException("Le passeport est obligatoire pour créer une carte résident");
        }

        // Générer numeroCarte unique (ex: "CRD-2026-" + timestamp)
        String numeroCarte = "CRD-" + System.currentTimeMillis();

        // Vérifier que le numéro ne existe pas déjà
        CarteResident existing = carteResidentRepository.findByNumeroCarte(numeroCarte);
        if (existing != null) {
            throw new BusinessException("Le numéro de carte existe déjà : " + numeroCarte);
        }

        CarteResident carte = new CarteResident();
        carte.setNumeroCarte(numeroCarte);
        carte.setDateDebut(java.time.LocalDate.now());
        carte.setDateFin(null);
        carte.setDemande(demande);
        carte.setPasseport(passeport);

        return carteResidentRepository.save(carte);
    }

    /**
     * Récupérer une carte résident par id_demande
     */
    public CarteResident findByDemandeId(Long idDemande) {
        return carteResidentRepository.findByDemandeId(idDemande);
    }

    /**
     * Récupérer une carte résident par numero_carte
     */
    public CarteResident findByNumeroCarte(String numeroCarte) {
        return carteResidentRepository.findByNumeroCarte(numeroCarte);
    }
}
