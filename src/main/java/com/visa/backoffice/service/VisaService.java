package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Visa;
import com.visa.backoffice.model.VisaPasseport;
import com.visa.backoffice.dto.VisaDTO;
import com.visa.backoffice.repository.VisaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class VisaService {

    private final VisaRepository visaRepository;
    private final VisaPasseportService visaPasseportService;

    public VisaService(VisaRepository visaRepository, VisaPasseportService visaPasseportService) {
        this.visaRepository = visaRepository;
        this.visaPasseportService = visaPasseportService;
    }

    /**
     * Créer un nouveau visa et son association visa_passeport.
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

        // Créer le visa sans les références directes à passeport et demande
        Visa visa = new Visa();
        visa.setReferenceVisa("VISA-" + LocalDateTime.now().toString().replace(':', '-').replace('.', '-'));
        visa.setDateDebut(LocalDate.now());
        visa.setDateFin(null);
        Visa savedVisa = visaRepository.save(visa);

        // Créer l'association dans visa_passeport
        visaPasseportService.creer(savedVisa, passeport, demande);

        return savedVisa;
    }

    /**
     * Crée un visa à partir des informations saisies pour un transfert sans antécédent.
     * Les dates sont imposées par le formulaire.
     */
    public Visa creer(Demande demande, Passeport passeport, VisaDTO visaDTO) {
        if (visaDTO == null) {
            return creer(demande, passeport);
        }
        if (visaDTO.getDateDebut() == null || visaDTO.getDateFin() == null) {
            throw new BusinessException("Les dates du visa à transférer sont obligatoires");
        }
        if (visaDTO.getDateFin().isBefore(visaDTO.getDateDebut())) {
            throw new BusinessException("La date de fin du visa doit être postérieure à la date de début");
        }

        Visa visa = new Visa();
        visa.setReferenceVisa("VISA-" + LocalDateTime.now().toString().replace(':', '-').replace('.', '-'));
        visa.setDateDebut(visaDTO.getDateDebut());
        visa.setDateFin(visaDTO.getDateFin());
        Visa savedVisa = visaRepository.save(visa);

        visaPasseportService.creer(savedVisa, passeport, demande);
        return savedVisa;
    }

    /**
     * Désactiver un visa (marquer comme inactif avec date_fin)
     * RG-03 : appelé en TRANSFERT pour marquer l'ancien visa comme inactif
     *
     * @param idDemande The demande whose active visa should be deactivated
     * @throws BusinessException if no active visa found
     */
    public void desactiver(Long idDemande) {
        // Trouver le visa actif associé à cette demande
        VisaPasseport activeAssociation = visaPasseportService.findActiveVisaByDemandeId(idDemande);
        if (activeAssociation == null) {
            throw new BusinessException("Aucun visa actif trouvé pour la demande id=" + idDemande);
        }

        Visa visaActif = activeAssociation.getVisa();
        visaActif.setDateFin(LocalDate.now());
        visaRepository.save(visaActif);
    }

    /**
     * Récupérer un visa par id
     */
    public Visa findById(Long idVisa) {
        return visaRepository.findById(idVisa).orElse(null);
    }

    /**
     * Récupérer le visa actif pour une demande
     */
    public Visa findActiveVisaByDemandeId(Long idDemande) {
        VisaPasseport association = visaPasseportService.findActiveVisaByDemandeId(idDemande);
        return association != null ? association.getVisa() : null;
    }

    /**
     * Récupérer les associations visa_passeport pour une demande
     */
    public VisaPasseport getVisaPasseportByDemande(Long idDemande) {
        return visaPasseportService.findActiveVisaByDemandeId(idDemande);
    }
}
