package com.visa.backoffice.controller;

import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.dto.PieceDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.repository.NationaliteRepository;
import com.visa.backoffice.repository.SituationFamilialeRepository;
import com.visa.backoffice.repository.TypeVisaRepository;
import com.visa.backoffice.service.DemandeService;
import com.visa.backoffice.service.PieceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeService demandeService;
    private final TypeVisaRepository typeVisaRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;
    private final PieceService pieceService;

    public DemandeController(DemandeService demandeService,
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
     * GET /demandes/nouvelle
     * Afficher le formulaire vierge
     */
    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        model.addAttribute("demandeForm", new DemandeCreateDTO());
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        return "demande/formulaire";
    }

    /**
     * GET /demandes/pieces?idTypeVisa=1
     * AJAX : retourner les pièces spécifiques en JSON
     */
    @GetMapping("/pieces")
    @ResponseBody
    public ResponseEntity<List<PieceDTO>> getPiecesParTypeVisa(@RequestParam Long idTypeVisa) {
        List<PieceDTO> pieces = demandeService.getPiecesFormulaire(idTypeVisa);
        return ResponseEntity.ok(pieces);
    }

    /**
     * POST /demandes/nouvelle
     * Soumettre et enregistrer la demande
     */
    @PostMapping("/nouvelle")
    public String soumettreFormulaire(
            @Valid @ModelAttribute("demandeForm") DemandeCreateDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Si erreurs de validation Bean Validation (@NotBlank, @NotNull, etc.)
        if (result.hasErrors()) {
            // Recharger les listes du formulaire
            model.addAttribute("typesVisa", typeVisaRepository.findAll());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
            return "demande/formulaire";
        }

        try {
            DemandeResponseDTO response = demandeService.creerDemande(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Demande créée avec succès. Référence : #" + response.getId());
            return "redirect:/demandes/" + response.getId() + "/confirmation";

        } catch (BusinessException e) {
            // Erreur métier (pièce manquante, référence dupliquée, etc.)
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("typesVisa", typeVisaRepository.findAll());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
            return "demande/formulaire";
        }
    }

    /**
     * GET /demandes/{id}/confirmation
     * Page de confirmation post-création
     */
    @GetMapping("/{id}/confirmation")
    public String afficherConfirmation(@PathVariable Long id, Model model) {
        DemandeResponseDTO demande = demandeService.getDemande(id);
        model.addAttribute("demande", demande);
        return "demande/confirmation";
    }
}
