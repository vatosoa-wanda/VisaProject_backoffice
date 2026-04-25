package com.visa.backoffice.controller;

import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.PieceDTO;
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
                                    @RequestParam(required = false) Long idTypeVisa,
                                    Model model) {
        throw new UnsupportedOperationException("À implémenter par Dev2");
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
            Model model) {
        throw new UnsupportedOperationException("À implémenter par Dev2");
    }

    /**
     * GET /duplicata/{id}/confirmation
     * Afficher la page de confirmation après soumission
     */
    @GetMapping("/{id}/confirmation")
    public String afficherConfirmation(@PathVariable Long id, Model model) {
        throw new UnsupportedOperationException("À implémenter par Dev2");
    }
}
