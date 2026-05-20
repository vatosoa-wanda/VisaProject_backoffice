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
    private final com.visa.backoffice.service.VisaPasseportService visaPasseportService;



    public TransfertController(DemandeService demandeService,

                             TypeVisaRepository typeVisaRepository,

                             SituationFamilialeRepository situationFamilialeRepository,

                             NationaliteRepository nationaliteRepository,
                             PieceService pieceService,
                             com.visa.backoffice.service.VisaPasseportService visaPasseportService) {
        this.demandeService = demandeService;

        this.typeVisaRepository = typeVisaRepository;

        this.situationFamilialeRepository = situationFamilialeRepository;

        this.nationaliteRepository = nationaliteRepository;

        this.pieceService = pieceService;
        this.visaPasseportService = visaPasseportService;
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

        DemandeCreateDTO demandeForm;
        if (idDemandeOrigine != null) {
            demandeForm = demandeService.getDemandePourModification(idDemandeOrigine);
            demandeForm.setIdDemandeOrigine(idDemandeOrigine);
            demandeForm.setTypeDemande("TRANSFERT");
            demandeForm.setAvecAntecedent(true);
            model.addAttribute("origine", demandeService.getDemande(idDemandeOrigine));
            if (demandeForm.getIdTypeVisa() != null) {
                model.addAttribute("piecesSpecifiques", demandeService.getPiecesFormulaire(demandeForm.getIdTypeVisa()));
            }
            // Remplacer les données héritées du visa_transformable par les données
            // du visa (table `visa`) associé via `visa_passeport` si disponible.
            try {
                var assoc = visaPasseportService.findAnyVisaByDemandeId(idDemandeOrigine);
                if (assoc != null && assoc.getVisa() != null) {
                    var visa = assoc.getVisa();
                    // Mapper les champs du visa vers le DTO attendu par le formulaire
                    var visaDTO = com.visa.backoffice.dto.VisaTransformableDTO.builder()
                            .referenceVisa(visa.getReferenceVisa())
                            .dateEntree(visa.getDateDebut())
                            .dateExpiration(visa.getDateFin())
                            .build();
                    demandeForm.setVisaDTO(visaDTO);
                }
            } catch (Exception ex) {
                // En cas d'erreur, on ignore et on conserve les données existantes
                System.err.println("Impossible de récupérer le visa via visa_passeport: " + ex.getMessage());
            }
        } else {
            demandeForm = DemandeCreateDTO.builder()
                    .typeDemande("TRANSFERT")
                    .avecAntecedent(avecAntecedent != null ? avecAntecedent : false)
                    .build();
        }

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
        
        System.out.println("=== SOUMISSION FORMULAIRE TRANSFERT ===");
        System.out.println("Type demande: " + dto.getTypeDemande());
        System.out.println("Avec antécédent: " + dto.getAvecAntecedent());
        System.out.println("Id demande origine: " + dto.getIdDemandeOrigine());
        
        // NE PAS utiliser result.getFieldErrors().clear() car c'est une liste immuable
        // On va ignorer les erreurs des champs hérités en mode AVEC antécédent
        
        boolean hasRealErrors = false;
        
        if (Boolean.TRUE.equals(dto.getAvecAntecedent())) {
            // En mode AVEC antécédent, on valide uniquement:
            // - le nouveau passeport
            // - le type de visa
            // - les pièces
            if (dto.getPasseportNouveauDTO() == null) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "Les informations du nouveau passeport sont obligatoires");
            }
            // Pour le cas AVEC antécédent, les informations du visa proviennent de la demande d'origine
            if (dto.getVisaDTO() == null) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "Les informations du visa à transférer sont obligatoires");
            } else {
                // Ne pas exiger systématiquement les dates ici : le visa en base peut être actif
                // (dateExpiration == null). Si les deux dates sont présentes, on vérifie l'ordre.
                if (dto.getVisaDTO().getDateEntree() != null && dto.getVisaDTO().getDateExpiration() != null
                        && dto.getVisaDTO().getDateExpiration().isBefore(dto.getVisaDTO().getDateEntree())) {
                    hasRealErrors = true;
                    model.addAttribute("errorMessage", "La date d'expiration du visa doit être postérieure à la date d'entrée");
                }
            }
            if (dto.getIdTypeVisa() == null) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "Le type de visa est obligatoire");
            }
        } else {
            // En mode SANS antécédent, on valide tout normalement
            // Simuler la validation manuellement
            if (dto.getDemandeurDTO() == null || dto.getDemandeurDTO().getNom() == null || dto.getDemandeurDTO().getNom().isBlank()) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "Le nom du demandeur est obligatoire");
            }
            if (dto.getPasseportDTO() == null || dto.getPasseportDTO().getNumero() == null || dto.getPasseportDTO().getNumero().isBlank()) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "Le numéro de passeport est obligatoire");
            }
            if (dto.getVisaDTO() == null || dto.getVisaDTO().getReferenceVisa() == null || dto.getVisaDTO().getReferenceVisa().isBlank()) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "La référence visa est obligatoire");
            }
            if (dto.getPasseportNouveauDTO() == null) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "Les informations du nouveau passeport sont obligatoires");
            }
            if (dto.getIdTypeVisa() == null) {
                hasRealErrors = true;
                model.addAttribute("errorMessage", "Le type de visa est obligatoire");
            }
        }
        
        if (hasRealErrors) {
            model.addAttribute("typeVisas", typeVisaRepository.findAll());
            model.addAttribute("typesVisa", typeVisaRepository.findAll());
            model.addAttribute("situations", situationFamilialeRepository.findAll());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
            model.addAttribute("piecesSpecifiques", dto.getIdTypeVisa() != null ? demandeService.getPiecesFormulaire(dto.getIdTypeVisa()) : List.of());
            model.addAttribute("formAction", "/transfert/formulaire");
            model.addAttribute("submitLabel", "Créer le transfert");
            model.addAttribute("cancelUrl", "/");
            model.addAttribute("demandeForm", dto);
            model.addAttribute("avecAntecedent", dto.getAvecAntecedent());
            model.addAttribute("modeAvecAntecedent", Boolean.TRUE.equals(dto.getAvecAntecedent()));
            model.addAttribute("modeSansAntecedent", Boolean.FALSE.equals(dto.getAvecAntecedent()));
            model.addAttribute("afficherRecherche", Boolean.TRUE.equals(dto.getAvecAntecedent()) && dto.getIdDemandeOrigine() == null);
            return "demande/formulaire";
        }



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
            DemandeResponseDTO created = null;
            
            // ========== CAS SANS ANTÉCÉDENT ==========
            if (Boolean.FALSE.equals(dto.getAvecAntecedent())) {
                System.out.println("=== CAS TRANSFERT SANS ANTÉCÉDENT ===");
                
                System.out.println("1. Création de la demande NOUVELLE...");
                DemandeResponseDTO nouvelleDemande = demandeService.creerDemande(dto);
                System.out.println("   → Demande NOUVELLE ID: " + nouvelleDemande.getId());
                
                System.out.println("2. Approbation automatique...");
                demandeService.approuverDemandeNouvelle(nouvelleDemande.getId(), null, true, dto.getVisaTransferDTO());
                System.out.println("   → Demande approuvée avec succès");
                
                System.out.println("3. Création du TRANSFERT...");
                created = demandeService.creerTransfertSansAntecedent(
                        nouvelleDemande.getId(),
                        dto.getPasseportNouveauDTO(),
                        dto.getPiecesFournies()
                );
                System.out.println("   → TRANSFERT créé ID: " + created.getId());
                
                redirectAttributes.addFlashAttribute("successMessage", "Transfert créé avec succès");
                return "redirect:/transfert/" + created.getId() + "/confirmation";
            }
            
            // ========== CAS AVEC ANTÉCÉDENT ==========
            if (Boolean.TRUE.equals(dto.getAvecAntecedent()) && dto.getIdDemandeOrigine() != null) {
                System.out.println("=== CAS TRANSFERT AVEC ANTÉCÉDENT ===");
                System.out.println("idDemandeOrigine: " + dto.getIdDemandeOrigine());
                
                TransfertCreateDTO transfertDto = TransfertCreateDTO.builder()
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

