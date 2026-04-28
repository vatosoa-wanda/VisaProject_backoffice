package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Visa;
import com.visa.backoffice.repository.VisaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
            throw new BusinessException("Passeport requis pour créer un visa");
        }

        Visa visa = new Visa();
        visa.setReferenceVisa("VISA-" + LocalDateTime.now().toString().replace(':', '-').replace('.', '-'));
        visa.setDateDebut(LocalDate.now());
        visa.setDateFin(null);
        visa.setPasseport(passeport);
        visa.setDemande(demande);
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
        Visa visaActif = visaRepository.findByDemandeId(idDemande);
        if (visaActif == null || visaActif.getDateFin() != null) {
            throw new BusinessException("Aucun visa actif trouvé pour la demande id=" + idDemande);
        }

        visaActif.setDateFin(LocalDate.now());
        visaRepository.save(visaActif);
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
