package com.visa.backoffice.controller;

import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.service.DemandeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
public class DemandeRestController {

    private final DemandeService demandeService;

    public DemandeRestController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    /**
     * GET /api/demandes
     * Retourner toutes les demandes en JSON
     */
    @GetMapping
    public ResponseEntity<List<DemandeResponseDTO>> getToutesDemandes() {
        return ResponseEntity.ok(demandeService.getToutesDemandes());
    }

    /**
     * GET /api/demandes/{id}
     * Retourner une demande par ID en JSON
     */
    @GetMapping("/{id}")
    public ResponseEntity<DemandeResponseDTO> getDemande(@PathVariable Long id) {
        return ResponseEntity.ok(demandeService.getDemande(id));
    }

    /**
     * POST /api/demandes
     * Créer une nouvelle demande
     */
    @PostMapping
    public ResponseEntity<DemandeResponseDTO> creerDemande(@Valid @RequestBody DemandeCreateDTO dto) {
        DemandeResponseDTO demande = demandeService.creerDemande(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(demande);
    }

    /**
     * DELETE /api/demandes/{id}
     * Supprimer une demande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDemande(@PathVariable Long id) {
        demandeService.supprimerDemande(id);
        return ResponseEntity.noContent().build();
    }
}
