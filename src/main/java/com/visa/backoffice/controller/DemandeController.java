package com.visa.backoffice.controller;



import com.visa.backoffice.dto.DemandeCreateDTO;

import com.visa.backoffice.dto.DemandeResponseDTO;

import com.visa.backoffice.dto.PieceDTO;

import com.visa.backoffice.exception.BusinessException;

import com.visa.backoffice.repository.NationaliteRepository;

import com.visa.backoffice.repository.SituationFamilialeRepository;

import com.visa.backoffice.repository.StatutDemandeRepository;

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

    private final StatutDemandeRepository statutDemandeRepository;



    public DemandeController(DemandeService demandeService,

                            TypeVisaRepository typeVisaRepository,

                            SituationFamilialeRepository situationFamilialeRepository,

                            NationaliteRepository nationaliteRepository,

                            PieceService pieceService,

                            StatutDemandeRepository statutDemandeRepository) {

        this.demandeService = demandeService;

        this.typeVisaRepository = typeVisaRepository;

        this.situationFamilialeRepository = situationFamilialeRepository;

        this.nationaliteRepository = nationaliteRepository;

        this.pieceService = pieceService;

        this.statutDemandeRepository = statutDemandeRepository;

    }



    /**

     * GET /demandes/nouvelle

     * Afficher le formulaire vierge

     */

    @GetMapping("/nouvelle")

    public String afficherFormulaire(@RequestParam(required = false) Long idTypeVisa, Model model) {

        DemandeCreateDTO form = new DemandeCreateDTO();

        form.setIdTypeVisa(idTypeVisa);



        model.addAttribute("demandeForm", form);

        model.addAttribute("typesVisa", typeVisaRepository.findAll());

        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

        model.addAttribute("nationalites", nationaliteRepository.findAll());

        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

        model.addAttribute("piecesSpecifiques", idTypeVisa != null ? demandeService.getPiecesFormulaire(idTypeVisa) : List.of());

        model.addAttribute("pageTitle", "Nouvelle demande de visa transformable");

        model.addAttribute("formAction", "/demandes/nouvelle");

        model.addAttribute("submitLabel", "ENREGISTRER");

        model.addAttribute("cancelUrl", "/demandes");

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

            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());

            model.addAttribute("pageTitle", "Nouvelle demande de visa transformable");

            model.addAttribute("formAction", "/demandes/nouvelle");

            model.addAttribute("submitLabel", "ENREGISTRER");

            model.addAttribute("cancelUrl", "/demandes");

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

            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());

            model.addAttribute("pageTitle", "Nouvelle demande de visa transformable");

            model.addAttribute("formAction", "/demandes/nouvelle");

            model.addAttribute("submitLabel", "ENREGISTRER");

            model.addAttribute("cancelUrl", "/demandes");

            return "demande/formulaire";

        }

    }



    /**

     * GET /demandes/{id}/modifier

     * Afficher le formulaire pré-rempli pour modification

     */

    @GetMapping("/{id}/modifier")

    public String afficherFormulaireModification(@PathVariable Long id, Model model) {

        DemandeCreateDTO form = demandeService.getDemandePourModification(id);

        model.addAttribute("demandeForm", form);

        model.addAttribute("typesVisa", typeVisaRepository.findAll());

        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

        model.addAttribute("nationalites", nationaliteRepository.findAll());

        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

        model.addAttribute("piecesSpecifiques", form.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(form.getIdTypeVisa()) : List.of());

        model.addAttribute("pageTitle", "Modifier la demande #" + id);

        model.addAttribute("formAction", "/demandes/" + id + "/modifier");

        model.addAttribute("submitLabel", "METTRE À JOUR");

        model.addAttribute("cancelUrl", "/demandes/" + id + "/details");

        return "demande/formulaire";

    }



    /**

     * POST /demandes/{id}/modifier

     * Enregistrer les modifications depuis le formulaire

     */

    @PostMapping("/{id}/modifier")

    public String soumettreModification(

            @PathVariable Long id,

            @Valid @ModelAttribute("demandeForm") DemandeCreateDTO dto,

            BindingResult result,

            Model model,

            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            model.addAttribute("typesVisa", typeVisaRepository.findAll());

            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

            model.addAttribute("nationalites", nationaliteRepository.findAll());

            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());

            model.addAttribute("pageTitle", "Modifier la demande #" + id);

            model.addAttribute("formAction", "/demandes/" + id + "/modifier");

            model.addAttribute("submitLabel", "METTRE À JOUR");

            model.addAttribute("cancelUrl", "/demandes/" + id + "/details");

            return "demande/formulaire";

        }



        try {

            demandeService.modifierDemande(id, dto);

            redirectAttributes.addFlashAttribute("successMessage", "Demande mise à jour avec succès");

            return "redirect:/demandes/" + id + "/details";

        } catch (BusinessException e) {

            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("typesVisa", typeVisaRepository.findAll());

            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

            model.addAttribute("nationalites", nationaliteRepository.findAll());

            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());

            model.addAttribute("pageTitle", "Modifier la demande #" + id);

            model.addAttribute("formAction", "/demandes/" + id + "/modifier");

            model.addAttribute("submitLabel", "METTRE À JOUR");

            model.addAttribute("cancelUrl", "/demandes/" + id + "/details");

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



    /**

     * GET /demandes

     * Afficher la liste de toutes les demandes

     */

    @GetMapping

    public String afficherListe(Model model) {

        model.addAttribute("demandes", demandeService.getToutesDemandes());

        return "demande/liste";

    }



    /**

     * GET /demandes/{id}/details

     * Afficher les détails d'une demande

     */

    @GetMapping("/{id}/details")

    public String afficherDetails(@PathVariable Long id, Model model) {

        DemandeResponseDTO demande = demandeService.getDemande(id);

        model.addAttribute("demande", demande);

        return "demande/details";

    }



    /**

     * GET /demandes/{id}/supprimer

     * Page de confirmation de suppression (sans JavaScript)

     */

    @GetMapping("/{id}/supprimer")

    public String confirmerSuppression(@PathVariable Long id, Model model) {

        DemandeResponseDTO demande = demandeService.getDemande(id);

        model.addAttribute("demande", demande);

        return "demande/confirmation-suppression";

    }



    /**

     * DELETE /demandes/{id}

     * Supprimer une demande

     */

    @DeleteMapping("/{id}")

    @ResponseBody

    public ResponseEntity<Void> supprimerDemande(@PathVariable Long id) {

        demandeService.supprimerDemande(id);

        return ResponseEntity.noContent().build();

    }



    /**

     * POST /demandes/{id}/supprimer

     * Supprimer une demande (version POST pour formulaire HTML)

     */

    @PostMapping("/{id}/supprimer")

    public String supprimerDemandeForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            demandeService.supprimerDemande(id);

            redirectAttributes.addFlashAttribute("successMessage", "Demande supprimée avec succès");

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression : " + e.getMessage());

        }

        return "redirect:/demandes";

    }

}

