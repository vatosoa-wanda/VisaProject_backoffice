package com.visa.backoffice.controller;

import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.repository.NationaliteRepository;
import com.visa.backoffice.repository.SituationFamilialeRepository;
import com.visa.backoffice.service.DemandeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeService demandeService;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;

    public DemandeController(DemandeService demandeService,
                            SituationFamilialeRepository situationFamilialeRepository,
                            NationaliteRepository nationaliteRepository) {
        this.demandeService = demandeService;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.nationaliteRepository = nationaliteRepository;
    }

    /**
     * GET /demandes/nouvelle
     * Afficher le formulaire vierge
     */
    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        model.addAttribute("demandeForm", new DemandeCreateDTO());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());
        return "demande/formulaire";
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
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            return "demande/formulaire";
        }

        try {
            DemandeResponseDTO response = demandeService.creerDemande(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Dossier civil enregistré avec succès. Référence : #" + response.getId());
            redirectAttributes.addFlashAttribute("demande", response);
            return "redirect:/demandes/confirmation";

        } catch (BusinessException e) {
            // Erreur métier (pièce manquante, référence dupliquée, etc.)
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            return "demande/formulaire";
        }
    }

    /**
     * GET /demandes/confirmation
     * Page de confirmation post-création
     */
    @GetMapping("/confirmation")
    public String afficherConfirmation(Model model) {
        if (!model.containsAttribute("demande")) {
            return "redirect:/demandes/nouvelle";
        }
        return "demande/confirmation";
    }
}
