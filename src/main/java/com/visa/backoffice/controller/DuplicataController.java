package com.visa.backoffice.controller;

import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.dto.DemandeRechercheDTO;
import com.visa.backoffice.dto.DemandeResumeeDTO;
import com.visa.backoffice.dto.DuplicataCreateDTO;
import com.visa.backoffice.dto.PieceDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.repository.NationaliteRepository;
import com.visa.backoffice.repository.SituationFamilialeRepository;
import com.visa.backoffice.repository.TypeVisaRepository;
import com.visa.backoffice.service.PieceService;
import com.visa.backoffice.service.DemandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * DuplicataController
 * Dev2 implémentera les routes pour DUPLICATA
 * Routes :
 *   GET /duplicata/formulaire     → Afficher formulaire avec choix antécédent
 *   POST /duplicata/formulaire    → Soumettre demande DUPLICATA
 *   GET /duplicata/{id}/confirmation → Afficher confirmation
 */
@Controller
@RequestMapping("/duplicata")
public class DuplicataController {

    private final DemandeService demandeService;
    private final TypeVisaRepository typeVisaRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;
    private final PieceService pieceService;

    public DuplicataController(DemandeService demandeService,
                             TypeVisaRepository typeVisaRepository,
                             SituationFamilialeRepository situationFamilialeRepository,
                             NationaliteRepository nationaliteRepository,
                             PieceService pieceService) {
        this.demandeService = demandeService;
        this.typeVisaRepository = typeVisaRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.nationaliteRepository = nationaliteRepository;
        this.pieceService = pieceService;
    }

    /**
     * GET /duplicata/formulaire
     * Afficher le formulaire de duplicata (avec choix antécédent si applicable)
     */
    @GetMapping("/formulaire")
    public String afficherFormulaire(@RequestParam(required = false) Long idDemandeOrigine,
                                    @RequestParam(required = false) Boolean avecAntecedent,
                                    @RequestParam(required = false) Long idTypeVisa,
                                    @RequestParam(required = false) Boolean rechercher,
                                    @ModelAttribute("recherche") DemandeRechercheDTO recherche,
                                    Model model) {
        // Listes communes pour le formulaire
        model.addAttribute("typeVisas", typeVisaRepository.findAll());
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situations", situationFamilialeRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        model.addAttribute("piecesSpecifiques", idTypeVisa != null ? demandeService.getPiecesFormulaire(idTypeVisa) : List.of());
        model.addAttribute("formAction", "/duplicata/formulaire");
        model.addAttribute("submitLabel", "Créer le duplicata");
        model.addAttribute("cancelUrl", "/");
        model.addAttribute("avecAntecedent", avecAntecedent);

        boolean modeAvecAntecedent = Boolean.TRUE.equals(avecAntecedent);
        boolean modeSansAntecedent = Boolean.FALSE.equals(avecAntecedent);
        model.addAttribute("modeAvecAntecedent", modeAvecAntecedent);
        model.addAttribute("modeSansAntecedent", modeSansAntecedent);
        model.addAttribute("afficherRecherche", modeAvecAntecedent && idDemandeOrigine == null);

        DemandeCreateDTO demandeForm;
        if (idDemandeOrigine != null) {
            demandeForm = demandeService.getDemandePourModification(idDemandeOrigine);
            demandeForm.setIdDemandeOrigine(idDemandeOrigine);
            demandeForm.setTypeDemande("DUPLICATA");
            demandeForm.setAvecAntecedent(true);
            model.addAttribute("origine", demandeService.getDemande(idDemandeOrigine));
            if (demandeForm.getIdTypeVisa() != null) {
                model.addAttribute("piecesSpecifiques", demandeService.getPiecesFormulaire(demandeForm.getIdTypeVisa()));
            }
        } else {
            demandeForm = DemandeCreateDTO.builder()
                    .typeDemande("DUPLICATA")
                    .avecAntecedent(false)
                    .build();
        }

        model.addAttribute("demandeForm", demandeForm);

        if (modeAvecAntecedent && idDemandeOrigine == null && Boolean.TRUE.equals(rechercher)) {
            try {
                model.addAttribute("resultatsRecherche", demandeService.rechercherDemandesApprouvees(recherche));
                model.addAttribute("rechercheEffectuee", true);
            } catch (BusinessException exception) {
                model.addAttribute("errorMessage", exception.getMessage());
                model.addAttribute("rechercheEffectuee", true);
                model.addAttribute("resultatsRecherche", List.of());
            }
        }

        return "demande/formulaire";
    }

    /**
     * GET /duplicata/pieces?idTypeVisa=1
     * AJAX : retourner les pièces spécifiques en JSON
     */
    @GetMapping("/pieces")
    @ResponseBody
    public ResponseEntity<List<PieceDTO>> getPiecesParTypeVisa(@RequestParam Long idTypeVisa) {
        List<PieceDTO> pieces = demandeService.getPiecesFormulaire(idTypeVisa);
        return ResponseEntity.ok(pieces);
    }

    /**
     * POST /duplicata/formulaire
     * Soumettre et enregistrer la demande de duplicata
     */
    @PostMapping("/formulaire")
    public String soumettreFormulaire(
            @Valid @ModelAttribute("demandeForm") DemandeCreateDTO dto,
            BindingResult result,
            @RequestParam(required = false) String refreshPieces,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("typeVisas", typeVisaRepository.findAll());
            model.addAttribute("typesVisa", typeVisaRepository.findAll());
            model.addAttribute("situations", situationFamilialeRepository.findAll());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());
            model.addAttribute("formAction", "/duplicata/formulaire");
            model.addAttribute("submitLabel", "Créer le duplicata");
            model.addAttribute("cancelUrl", "/");
            return "demande/formulaire";
        }

        // Refresh pieces action (user clicked "Mettre à jour les pièces spécifiques")
        if (refreshPieces != null) {
            model.addAttribute("typeVisas", typeVisaRepository.findAll());
            model.addAttribute("typesVisa", typeVisaRepository.findAll());
            model.addAttribute("situations", situationFamilialeRepository.findAll());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());
            model.addAttribute("formAction", "/duplicata/formulaire");
            model.addAttribute("submitLabel", "Créer le duplicata");
            model.addAttribute("cancelUrl", "/");
            model.addAttribute("demandeForm", dto);

            // Ensure the AVEC/SANS flags are present so the template shows the correct sections
            model.addAttribute("avecAntecedent", dto.getAvecAntecedent());
            boolean modeAvecAntecedent = Boolean.TRUE.equals(dto.getAvecAntecedent());
            boolean modeSansAntecedent = Boolean.FALSE.equals(dto.getAvecAntecedent());
            model.addAttribute("modeAvecAntecedent", modeAvecAntecedent);
            model.addAttribute("modeSansAntecedent", modeSansAntecedent);
            model.addAttribute("afficherRecherche", modeAvecAntecedent && dto.getIdDemandeOrigine() == null);

            return "demande/formulaire";
        }

        // Cas AVEC antécédent
        if (Boolean.TRUE.equals(dto.getAvecAntecedent()) && dto.getIdDemandeOrigine() != null) {
            DuplicataCreateDTO duplicataDto = DuplicataCreateDTO.builder()
                    .idDemandeOrigine(dto.getIdDemandeOrigine())
                    .piecesFournies(dto.getPiecesFournies())
                    .build();

            var created = demandeService.creerDuplicata(duplicataDto);
            return "redirect:/duplicata/" + created.getId() + "/confirmation";
        }

        // Cas SANS antécédent : NOUVELLE + approbation auto + DUPLICATA
        DemandeResponseDTO nouvelleDemande = demandeService.creerDemande(dto);
        demandeService.approuverDemandeNouvelle(nouvelleDemande.getId(), null, true);

        DuplicataCreateDTO duplicataAuto = DuplicataCreateDTO.builder()
                .idDemandeOrigine(nouvelleDemande.getId())
                .piecesFournies(dto.getPiecesFournies())
                .build();

        DemandeResponseDTO created = demandeService.creerDuplicata(duplicataAuto);
        return "redirect:/duplicata/" + created.getId() + "/confirmation";
    }

    // Backwards-compatible overload used by unit tests which call the controller method directly
    public String soumettreFormulaire(DemandeCreateDTO dto, BindingResult result, Model model) {
        return soumettreFormulaire(dto, result, null, model);
    }

    /**
     * GET /duplicata/{id}/confirmation
     * Afficher la page de confirmation après soumission
     */
    @GetMapping("/{id}/confirmation")
    public String afficherConfirmation(@PathVariable Long id, Model model) {
        var demande = demandeService.getDemande(id);
        model.addAttribute("demande", demande);
        return "duplicata/confirmation";
    }
}
