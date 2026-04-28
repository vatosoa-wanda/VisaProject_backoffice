package com.visa.backoffice.controller;

import com.visa.backoffice.dto.*;
import com.visa.backoffice.exception.ResourceNotFoundException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.VisaTransformable;
import com.visa.backoffice.repository.*;
import com.visa.backoffice.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/demandes/{id}/scan")
@Slf4j
public class ScanController {

    private final DemandeRepository demandeRepository;
    private final DocumentService documentService;
    private final PieceRepository pieceRepository;

    public ScanController(DemandeRepository demandeRepository,
                        DocumentService documentService,
                        PieceRepository pieceRepository) {
        this.demandeRepository = demandeRepository;
        this.documentService = documentService;
        this.pieceRepository = pieceRepository;
    }

    /**
     * GET /demandes/{id}/scan
     * Affiche la page de scan des pièces justificatives
     */
    @GetMapping
    public String afficherPageScan(@PathVariable Long id, Model model) {
        try {
            // Récupérer la demande
            Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

            // Extraire les informations
            Demandeur demandeur = demande.getDemandeur();
            Passeport passeport = null;
            if (demandeur != null && demandeur.getPasseports() != null && !demandeur.getPasseports().isEmpty()) {
                passeport = demandeur.getPasseports().iterator().next();
            }

            VisaTransformable visa = demande.getVisaTransformable();

            // Construire le DTO d'infos du demandeur
            DemandeurDTO demandeurDTO = demandeur != null ? DemandeurDTO.builder()
                .nom(demandeur.getNom())
                .prenom(demandeur.getPrenom())
                .dateNaissance(demandeur.getDateNaissance())
                .build()
                : null;

            // Construire le DTO du passeport
            PasseportDTO passeportDTO = passeport != null ? PasseportDTO.builder()
                .numero(passeport.getNumero())
                .dateDelivrance(passeport.getDateDelivrance())
                .dateExpiration(passeport.getDateExpiration())
                .build()
                : null;

            // Construire le DTO du visa
            VisaDTO visaDTO = visa != null ? VisaDTO.builder()
                .referenceVisa(visa.getReferenceVisa())
                .dateDebut(visa.getDateEntree())
                .dateFin(visa.getDateExpiration())
                .build()
                : null;

            // Récupérer les documents
            List<DocumentDTO> documents = documentService.getDocumentsByDemande(id);

            // Construire la page DTO
            String reference = "DEM-" + String.format("%06d", id);
            String statut = demande.getStatutDemande() != null ? demande.getStatutDemande().getLibelle() : "INCONNU";
            Boolean dossierVerrouille = "SCAN_TERMINE".equalsIgnoreCase(statut);

            ScanPageDTO pageDTO = ScanPageDTO.builder()
                .demandeId(id)
                .reference(reference)
                .statut(statut)
                .demandeur(demandeurDTO)
                .passeport(passeportDTO)
                .visa(visaDTO)
                .documents(documents)
                .nombreDocumentsFournis(documents.size())
                .nombreDocumentsAttendus(pieceRepository.findAll().size())
                .dossierVerrouille(dossierVerrouille)
                .build();

            model.addAttribute("scanPage", pageDTO);
            model.addAttribute("typePieces", pieceRepository.findAll());
            model.addAttribute("pageTitle", "Scan des pièces justificatives");

            log.info("Page scan affichée - demande: {}, statut: {}", id, statut);
            return "scan/scan";

        } catch (ResourceNotFoundException e) {
            log.error("Demande non trouvée: {}", id);
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        } catch (Exception e) {
            log.error("Erreur lors de l'affichage de la page scan", e);
            model.addAttribute("errorMessage", "Erreur lors du chargement de la page");
            return "error/500";
        }
    }

    /**
     * GET /demandes/{id}/scan/infos
     * Retourne les infos du scan en JSON (pour AJAX optionnel)
     */
    @GetMapping("/infos")
    @ResponseBody
    public ResponseEntity<ScanPageDTO> getScanInfos(@PathVariable Long id) {
        try {
            Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

            Demandeur demandeur = demande.getDemandeur();
            Passeport passeport = null;
            if (demandeur != null && demandeur.getPasseports() != null && !demandeur.getPasseports().isEmpty()) {
                passeport = demandeur.getPasseports().iterator().next();
            }
            VisaTransformable visa = demande.getVisaTransformable();

            DemandeurDTO demandeurDTO = demandeur != null ? DemandeurDTO.builder()
                .nom(demandeur.getNom())
                .prenom(demandeur.getPrenom())
                .dateNaissance(demandeur.getDateNaissance())
                .build()
                : null;

            PasseportDTO passeportDTO = passeport != null ? PasseportDTO.builder()
                .numero(passeport.getNumero())
                .dateExpiration(passeport.getDateExpiration())
                .build()
                : null;

            VisaDTO visaDTO = visa != null ? VisaDTO.builder()
                .referenceVisa(visa.getReferenceVisa())
                .build()
                : null;

            List<DocumentDTO> documents = documentService.getDocumentsByDemande(id);
            String statut = demande.getStatutDemande() != null ? demande.getStatutDemande().getLibelle() : "INCONNU";

            ScanPageDTO pageDTO = ScanPageDTO.builder()
                .demandeId(id)
                .reference("DEM-" + String.format("%06d", id))
                .statut(statut)
                .demandeur(demandeurDTO)
                .passeport(passeportDTO)
                .visa(visaDTO)
                .documents(documents)
                .nombreDocumentsFournis(documents.size())
                .dossierVerrouille("SCAN_TERMINE".equalsIgnoreCase(statut))
                .build();

            return ResponseEntity.ok(pageDTO);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

