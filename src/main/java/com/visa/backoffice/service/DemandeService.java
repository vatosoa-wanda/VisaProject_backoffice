package com.visa.backoffice.service;

<<<<<<< Updated upstream
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.repository.DemandeRepository;
import org.springframework.stereotype.Service;
=======
import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.repository.DemandeurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
>>>>>>> Stashed changes

@Service
public class DemandeService {

<<<<<<< Updated upstream
    private final DemandeRepository demandeRepository;

    public DemandeService(DemandeRepository demandeRepository) {
        this.demandeRepository = demandeRepository;
=======
    private final DemandeurRepository demandeurRepository;
    private final DemandeurService demandeurService;
    private final PasseportService passeportService;

    public DemandeService(
            DemandeurRepository demandeurRepository,
            DemandeurService demandeurService,
            PasseportService passeportService) {
        this.demandeurRepository = demandeurRepository;
        this.demandeurService = demandeurService;
        this.passeportService = passeportService;
    }

    /**
     * Crée un dossier limité aux fonctionnalités Demandeur + Passeport.
     * - Création/récupération du demandeur (doublon nom + date de naissance)
     * - Validation/creation du passeport lié
     */
    public DemandeResponseDTO creerDemande(DemandeCreateDTO dto) {
        boolean demandeurExistant = demandeurRepository
                .findByNomAndDateNaissance(
                        dto.getDemandeurDTO().getNom(),
                        dto.getDemandeurDTO().getDateNaissance())
                .isPresent();

        Demandeur demandeur = demandeurService.creerOuRecuperer(dto.getDemandeurDTO());
        Passeport passeport = passeportService.creer(dto.getPasseportDTO(), demandeur);

        DemandeResponseDTO response = new DemandeResponseDTO();
        response.setId(passeport.getId());
        response.setDateDemande(LocalDateTime.now());
        response.setNomDemandeur(demandeur.getNom());
        response.setPrenomDemandeur(demandeur.getPrenom());
        response.setNumeroPasSeport(passeport.getNumero());
        response.setTypeDemande(demandeurExistant ? "DEMANDEUR_EXISTANT" : "NOUVEAU_DEMANDEUR");
        response.setStatutDemande("ENREGISTRE");
        response.setDemandeurExistant(demandeurExistant);
        return response;
>>>>>>> Stashed changes
    }
}
