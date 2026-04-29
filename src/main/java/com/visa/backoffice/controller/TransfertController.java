package com.visa.backoffice.controller;

import com.visa.backoffice.dto.*;
import com.visa.backoffice.exception.BusinessException;
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
        model.addAttribute("formAction", "/transfert/formulaire");
        model.addAttribute("submitLabel", "Créer le transfert");
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
            demandeForm.setTypeDemande("TRANSFERT");
            demandeForm.setAvecAntecedent(true);
            model.addAttribute("origine", demandeService.getDemande(idDemandeOrigine));
            if (demandeForm.getIdTypeVisa() != null) {
                model.addAttribute("piecesSpecifiques", demandeService.getPiecesFormulaire(demandeForm.getIdTypeVisa()));
            }
        } else {
            demandeForm = DemandeCreateDTO.builder()
                    .typeDemande("TRANSFERT")
                    .avecAntecedent(false)
                    .build();
        }

        model.addAttribute("demandeForm", demandeForm);

        if (modeAvecAntecedent && idDemandeOrigine == null && Boolean.TRUE.equals(rechercher)) {
            try {
                List<DemandeResumeeDTO> results = demandeService.rechercherDemandesApprouveesPourTransfert(recherche);
                model.addAttribute("resultatsRecherche", results);
                model.addAttribute("rechercheEffectuee", true);
            } catch (BusinessException exception) {
                model.addAttribute("errorMessage", exception.getMessage());
                model.addAttribute("rechercheEffectuee", true);
                model.addAttribute("resultatsRecherche", List.of());
            }
        }

        return "demande/formulaire";
    }

    @GetMapping("/pieces")
    @ResponseBody
    public ResponseEntity<List<PieceDTO>> getPiecesParTypeVisa(@RequestParam Long idTypeVisa) {
        List<PieceDTO> pieces = demandeService.getPiecesFormulaire(idTypeVisa);
        return ResponseEntity.ok(pieces);
    }

    @PostMapping("/formulaire")
    public String soumettreFormulaire(
            @Valid @ModelAttribute("demandeForm") DemandeCreateDTO dto,
            BindingResult result,
            @RequestParam(required = false) String refreshPieces,
            Model model,
            RedirectAttributes redirectAttributes) {  // AJOUTER RedirectAttributes
        
        System.out.println("=== SOUMISSION FORMULAIRE TRANSFERT ===");
        System.out.println("Type demande: " + dto.getTypeDemande());
        System.out.println("Avec antécédent: " + dto.getAvecAntecedent());
        
        if (result.hasErrors()) {
            System.out.println("Erreurs de validation: " + result.getAllErrors());
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
            return "demande/formulaire";
        }

        if (refreshPieces != null) {
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
            return "demande/formulaire";
        }

        try {
            DemandeResponseDTO created = null;
            
            // ========== CAS SANS ANTÉCÉDENT ==========
            if (Boolean.FALSE.equals(dto.getAvecAntecedent())) {
                System.out.println("=== CAS TRANSFERT SANS ANTÉCÉDENT ===");
                
                if (dto.getPasseportNouveauDTO() == null) {
                    throw new BusinessException("Les informations du nouveau passeport sont obligatoires");
                }
                
                System.out.println("1. Création de la demande NOUVELLE...");
                DemandeResponseDTO nouvelleDemande = demandeService.creerDemande(dto);
                System.out.println("   → Demande NOUVELLE ID: " + nouvelleDemande.getId());
                
                System.out.println("2. Approbation automatique...");
                demandeService.approuverDemandeNouvelle(nouvelleDemande.getId(), null, true);
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
                
                created = demandeService.creerTransfert(transfertDto);
                System.out.println("   → TRANSFERT créé ID: " + created.getId());
                
                redirectAttributes.addFlashAttribute("successMessage", "Transfert créé avec succès");
                return "redirect:/transfert/" + created.getId() + "/confirmation";
            }
            
            throw new BusinessException("Configuration de transfert invalide");
            
        } catch (BusinessException e) {
            System.err.println("ERREUR BUSINESS: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
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
            return "demande/formulaire";
            
        } catch (Exception e) {
            System.err.println("ERREUR GENERALE: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Erreur technique: " + e.getMessage());
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
            return "demande/formulaire";
        }
    }

    @GetMapping("/{id}/confirmation")
    public String afficherConfirmation(@PathVariable Long id, Model model) {
        var demande = demandeService.getDemande(id);
        model.addAttribute("demande", demande);
        return "transfert/confirmation";
    }
}