package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Visa;
import com.visa.backoffice.repository.VisaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class VisaService {

    private final VisaRepository visaRepository;

    public VisaService(VisaRepository visaRepository) {
        this.visaRepository = visaRepository;
    }

    /**
     * Créer un nouveau visa
     * RG-01 : appelé après approbation d'une demande NOUVELLE
     * RG-07 : appelé pour créer un nouveau visa en TRANSFERT
     *
     * @param demande The demande to link with
     * @param passeport The passeport to link with
     * @return Created Visa with generated referenceVisa and dateDebut=today
     * @throws BusinessException if passeport is null
     */
    public Visa creer(Demande demande, Passeport passeport) {
        if (passeport == null) {
            throw new BusinessException("Le passeport est obligatoire pour créer un visa");
        }

        // Générer referenceVisa unique (ex: "VIS-2026-001-" + timestamp)
        String referenceVisa = "VIS-" + System.currentTimeMillis();

        Visa visa = new Visa();
        visa.setReferenceVisa(referenceVisa);
        visa.setDateDebut(LocalDate.now());
        visa.setDateFin(null);
        visa.setDemande(demande);
        visa.setPasseport(passeport);

        return visaRepository.save(visa);
    }

    /**
     * Désactiver un visa
     * RG-03 : appelé en TRANSFERT pour marquer l'ancien visa comme inactif
     *
     * @param idDemande The demande whose active visa should be deactivated
     * @throws BusinessException if no active visa found
     */
    public void desactiver(Long idDemande) {
        Visa visa = visaRepository.findByDemandeId(idDemande);
        if (visa == null) {
            throw new BusinessException("Aucun visa actif trouvé pour la demande : " + idDemande);
        }

        visa.setDateFin(LocalDate.now());
        visaRepository.save(visa);
    }

    /**
     * Récupérer un visa par id_demande
     */
    public Visa findByDemandeId(Long idDemande) {
        return visaRepository.findByDemandeId(idDemande);
    }

    /**
     * Récupérer un visa par id_passeport
     */
    public Visa findByPasseportId(Long idPasseport) {
        return visaRepository.findByPasseportId(idPasseport);
    }
}
