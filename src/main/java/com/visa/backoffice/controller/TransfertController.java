package com.visa.backoffice.controller;



import com.visa.backoffice.dto.DemandeCreateDTO;

import com.visa.backoffice.dto.PieceDTO;

import com.visa.backoffice.dto.TransfertCreateDTO;

import com.visa.backoffice.exception.BusinessException;

import com.visa.backoffice.exception.ResourceNotFoundException;

import com.visa.backoffice.repository.NationaliteRepository;

import com.visa.backoffice.repository.SituationFamilialeRepository;

import com.visa.backoffice.repository.TypeVisaRepository;

import com.visa.backoffice.service.DemandeService;

import com.visa.backoffice.service.PieceService;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;



import jakarta.validation.Valid;

import java.util.List;



/**

 * TransfertController

 * Dev1 implémentera les routes pour TRANSFERT

 * Routes :

 *   GET /transfert/formulaire     → Afficher formulaire avec choix antécédent

 *   POST /transfert/formulaire    → Soumettre demande TRANSFERT

 *   GET /transfert/{id}/confirmation → Afficher confirmation

 */

@Controller

@RequestMapping("/transfert")

public class TransfertController {



    private final DemandeService demandeService;

    private final TypeVisaRepository typeVisaRepository;

    private final SituationFamilialeRepository situationFamilialeRepository;

    private final NationaliteRepository nationaliteRepository;

    private final PieceService pieceService;



    public TransfertController(DemandeService demandeService,

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

     * GET /transfert/formulaire

     * Afficher le formulaire de transfert (avec choix antécédent si applicable)

     */

    @GetMapping("/formulaire")

    public String afficherFormulaire(@RequestParam(required = false) Long idDemandeOrigine,

                                    @RequestParam(required = false) Long idTypeVisa,

                                    Model model) {

        DemandeCreateDTO form = new DemandeCreateDTO();

        form.setTypeDemande("TRANSFERT");

        form.setIdTypeVisa(idTypeVisa);



        // CAS 2.1 : TRANSFERT AVEC ANTÉCÉDENT

        if (idDemandeOrigine != null) {

            form.setIdDemandeOrigine(idDemandeOrigine);

            form.setAvecAntecedent(true);



            // Pré-remplir avec les données de la demande d'origine

            try {

                com.visa.backoffice.dto.DemandeResponseDTO demandeOrigine = demandeService.getDemande(idDemandeOrigine);

                // L'extraction des données sera faite côté template via le dto

                model.addAttribute("demandeForm", form);

                model.addAttribute("demandeOrigineId", idDemandeOrigine);

            } catch (Exception e) {

                model.addAttribute("errorMessage", "Demande d'origine introuvable");

            }

        } else {

            // CAS 2.2 : TRANSFERT SANS ANTÉCÉDENT (non implémenté dans cette tâche)

            form.setAvecAntecedent(false);

            model.addAttribute("demandeForm", form);

        }



        model.addAttribute("demandeForm", form);

        model.addAttribute("typesVisa", typeVisaRepository.findAll());

        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

        model.addAttribute("nationalites", nationaliteRepository.findAll());

        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

        model.addAttribute("piecesSpecifiques", idTypeVisa != null ? demandeService.getPiecesFormulaire(idTypeVisa) : List.of());

        model.addAttribute("pageTitle", "Demande de Transfert");

        model.addAttribute("formAction", "/transfert/formulaire");

        model.addAttribute("submitLabel", "SOUMETTRE LE TRANSFERT");

        model.addAttribute("cancelUrl", "/demandes");

        return "demande/formulaire";

    }



    /**

     * GET /transfert/pieces?idTypeVisa=1

     * AJAX : retourner les pièces spécifiques en JSON

     */

    @GetMapping("/pieces")

    @ResponseBody

    public ResponseEntity<List<PieceDTO>> getPiecesParTypeVisa(@RequestParam Long idTypeVisa) {

        List<PieceDTO> pieces = demandeService.getPiecesFormulaire(idTypeVisa);

        return ResponseEntity.ok(pieces);

    }



    /**

     * POST /transfert/formulaire

     * Soumettre et enregistrer la demande de transfert

     */

    @PostMapping("/formulaire")

    public String soumettreFormulaire(

            @Valid @ModelAttribute("demandeForm") DemandeCreateDTO dto,

            BindingResult result,

            Model model,

            RedirectAttributes redirectAttributes) {



        // Si erreurs de validation Bean Validation

        if (result.hasErrors()) {

            model.addAttribute("typesVisa", typeVisaRepository.findAll());

            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

            model.addAttribute("nationalites", nationaliteRepository.findAll());

            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());

            model.addAttribute("pageTitle", "Demande de Transfert");

            model.addAttribute("formAction", "/transfert/formulaire");

            model.addAttribute("submitLabel", "SOUMETTRE LE TRANSFERT");

            model.addAttribute("cancelUrl", "/demandes");

            return "demande/formulaire";

        }



        try {

            // CAS 2.1 : TRANSFERT AVEC ANTÉCÉDENT

            if (dto.getIdDemandeOrigine() != null && dto.getAvecAntecedent() != null && dto.getAvecAntecedent()) {

                TransfertCreateDTO transfertDTO = TransfertCreateDTO.builder()

                        .idDemandeOrigine(dto.getIdDemandeOrigine())

                        .passeportNouveau(dto.getPasseportNouveauDTO())

                        .piecesFournies(dto.getPiecesFournies())

                        .build();



                var response = demandeService.creerTransfert(transfertDTO);

                redirectAttributes.addFlashAttribute("successMessage", "Transfert créé avec succès. Référence : #" + response.getId());

                return "redirect:/transfert/" + response.getId() + "/confirmation";

            }



            // Cas par défaut : erreur

            model.addAttribute("errorMessage", "Paramètres manquants pour le transfert");

            model.addAttribute("typesVisa", typeVisaRepository.findAll());

            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

            model.addAttribute("nationalites", nationaliteRepository.findAll());

            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());

            model.addAttribute("pageTitle", "Demande de Transfert");

            model.addAttribute("formAction", "/transfert/formulaire");

            model.addAttribute("submitLabel", "SOUMETTRE LE TRANSFERT");

            model.addAttribute("cancelUrl", "/demandes");

            return "demande/formulaire";



        } catch (BusinessException e) {

            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("typesVisa", typeVisaRepository.findAll());

            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());

            model.addAttribute("nationalites", nationaliteRepository.findAll());

            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());

            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());

            model.addAttribute("pageTitle", "Demande de Transfert");

            model.addAttribute("formAction", "/transfert/formulaire");

            model.addAttribute("submitLabel", "SOUMETTRE LE TRANSFERT");

            model.addAttribute("cancelUrl", "/demandes");

            return "demande/formulaire";

        }

    }



    /**

     * GET /transfert/{id}/confirmation

     * Afficher la page de confirmation après soumission

     */

    @GetMapping("/{id}/confirmation")

    public String afficherConfirmation(@PathVariable Long id, Model model) {

        try {

            var demande = demandeService.getDemande(id);

            model.addAttribute("demande", demande);

            model.addAttribute("successMessage", "Transfert créé avec succès");

            return "demande/confirmation";

        } catch (ResourceNotFoundException e) {

            model.addAttribute("errorMessage", "Demande introuvable");

            return "redirect:/demandes";

        }

    }

}

