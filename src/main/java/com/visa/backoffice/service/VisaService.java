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
        // Dev1 implémente ici
        // ✅ Valider passeport != null
        // ✅ Générer referenceVisa (ex: "VIS-" + timestamp)
        // ✅ Set dateDebut = LocalDate.now()
        // ✅ Set dateFin = null
        // ✅ Link à demande et passeport
        // ✅ Save et return
        throw new UnsupportedOperationException("À implémenter par Dev1");
    }

    /**
     * Désactiver un visa
     * RG-03 : appelé en TRANSFERT pour marquer l'ancien visa comme inactif
     *
     * @param idDemande The demande whose active visa should be deactivated
     * @throws BusinessException if no active visa found
     */
    public void desactiver(Long idDemande) {
        // Dev1 implémente ici
        // ✅ Charger visa actif de la demande (dateFin = null)
        // ✅ Set dateFin = LocalDate.now()
        // ✅ Save
        throw new UnsupportedOperationException("À implémenter par Dev1");
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
