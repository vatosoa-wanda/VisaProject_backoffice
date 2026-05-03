package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Visa;
import com.visa.backoffice.model.VisaPasseport;
import com.visa.backoffice.repository.VisaPasseportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VisaPasseportService {

    private final VisaPasseportRepository visaPasseportRepository;

    public VisaPasseportService(VisaPasseportRepository visaPasseportRepository) {
        this.visaPasseportRepository = visaPasseportRepository;
    }

    /**
     * Crée une association visa_passeport.
     * Appelée lors de la création d'un visa.
     *
     * @param visa       le visa créé
     * @param passeport  le passeport associé
     * @param demande    la demande associée
     * @return          VisaPasseport créée
     * @throws BusinessException si l'association existe déjà
     */
    public VisaPasseport creer(Visa visa, Passeport passeport, Demande demande) {
        if (visa == null || passeport == null || demande == null) {
            throw new BusinessException("Visa, Passeport et Demande sont requis");
        }

        // Vérifier qu'une association identique n'existe pas déjà
        VisaPasseport existant = visaPasseportRepository
                .findByVisaIdAndPasseportIdAndDemandeId(visa.getId(), passeport.getId(), demande.getId());
        if (existant != null) {
            throw new BusinessException("Une association visa_passeport existe déjà pour ce visa, passeport et demande");
        }

        VisaPasseport association = VisaPasseport.builder()
                .visa(visa)
                .passeport(passeport)
                .demande(demande)
                .dateAssociation(LocalDateTime.now())
                .build();

        return visaPasseportRepository.save(association);
    }

    /**
     * Récupère toutes les associations pour un visa donné
     *
     * @param idVisa l'id du visa
     * @return liste des VisaPasseport
     */
    public List<VisaPasseport> findByVisaId(Long idVisa) {
        return visaPasseportRepository.findByVisaId(idVisa);
    }

    /**
     * Récupère toutes les associations pour un passeport donné
     *
     * @param idPasseport l'id du passeport
     * @return liste des VisaPasseport
     */
    public List<VisaPasseport> findByPasseportId(Long idPasseport) {
        return visaPasseportRepository.findByPasseportId(idPasseport);
    }

    /**
     * Récupère toutes les associations pour une demande donnée
     *
     * @param idDemande l'id de la demande
     * @return liste des VisaPasseport
     */
    public List<VisaPasseport> findByDemandeId(Long idDemande) {
        return visaPasseportRepository.findByDemandeId(idDemande);
    }

    /**
     * Récupère une association spécifique
     *
     * @param idVisa     l'id du visa
     * @param idPasseport l'id du passeport
     * @param idDemande  l'id de la demande
     * @return VisaPasseport ou null si non trouvée
     */
    public VisaPasseport findByVisaIdAndPasseportIdAndDemandeId(Long idVisa, Long idPasseport, Long idDemande) {
        return visaPasseportRepository.findByVisaIdAndPasseportIdAndDemandeId(idVisa, idPasseport, idDemande);
    }

    /**
     * Supprime toutes les associations pour un visa
     *
     * @param idVisa l'id du visa
     */
    public void deleteByVisaId(Long idVisa) {
        visaPasseportRepository.deleteByVisaId(idVisa);
    }

    /**
     * Supprime toutes les associations pour une demande
     *
     * @param idDemande l'id de la demande
     */
    public void deleteByDemandeId(Long idDemande) {
        visaPasseportRepository.deleteByDemandeId(idDemande);
    }

    /**
     * Récupère le premier visa actif (date_fin = null) associé à une demande
     *
     * @param idDemande l'id de la demande
     * @return VisaPasseport du visa actif ou null
     */
    public VisaPasseport findActiveVisaByDemandeId(Long idDemande) {
        List<VisaPasseport> associations = findByDemandeId(idDemande);
        return associations.stream()
                .filter(assoc -> assoc.getVisa().getDateFin() == null)
                .findFirst()
                .orElse(null);
    }

    /**
     * Récupère le premier visa associé à une demande, sans filtre sur la date de fin.
     * Utile quand le visa possède déjà une date de fin saisie au formulaire.
     */
    public VisaPasseport findAnyVisaByDemandeId(Long idDemande) {
        List<VisaPasseport> associations = findByDemandeId(idDemande);
        return associations.stream()
                .findFirst()
                .orElse(null);
    }
}
