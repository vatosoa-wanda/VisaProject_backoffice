package com.visa.backoffice.controller;

import com.visa.backoffice.dto.DocumentDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.exception.DemandeVerrouilleException;
import com.visa.backoffice.exception.ResourceNotFoundException;
import com.visa.backoffice.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/documents")
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * POST /documents
     * Upload un nouveau document
     */
    @PostMapping
    public String uploadDocument(
            @RequestParam Long demandeId,
            @RequestParam Long pieceId,
            @RequestParam("fichier") MultipartFile fichier,
            RedirectAttributes redirectAttributes) {

        try {
            documentService.uploadDocument(demandeId, pieceId, fichier);
            redirectAttributes.addFlashAttribute("successMessage", "Document uploadé avec succès");
            log.info("Document uploadé - demande: {}, piece: {}", demandeId, pieceId);
        } catch (DemandeVerrouilleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            log.warn("Tentative d'upload sur demande verrouillée: {}", demandeId);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            log.error("Erreur métier lors de l'upload: {}", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'upload: " + e.getMessage());
            log.error("Erreur inattendue lors de l'upload", e);
        }

        return "redirect:/demandes/" + demandeId + "/scan";
    }

    /**
     * GET /documents?demandeId={id}
     * Liste les documents d'une demande (JSON)
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<DocumentDTO>> getDocumentsByDemande(@RequestParam Long demandeId) {
        try {
            List<DocumentDTO> documents = documentService.getDocumentsByDemande(demandeId);
            return ResponseEntity.ok(documents);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /documents/{id}/file
     * Télécharge/affiche un document
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        try {
            byte[] fileContent = documentService.getDocumentFile(id);

            // Détermine le content-type basé sur l'extension
            String contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(fileContent);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier: {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * DELETE /documents/{id}
     * Supprime un document
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.supprimerDocument(id);
            log.info("Document supprimé: {}", id);
            return ResponseEntity.noContent().build();
        } catch (DemandeVerrouilleException e) {
            log.warn("Tentative de suppression sur demande verrouillée: {}", id);
            return ResponseEntity.status(403).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /documents/{id}
     * Remplace un document existant
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<DocumentDTO> updateDocument(
            @PathVariable Long id,
            @RequestParam("fichier") MultipartFile fichier) {

        try {
            DocumentDTO updated = documentService.remplacerDocument(id, fichier);
            log.info("Document remplacé: {}", id);
            return ResponseEntity.ok(updated);
        } catch (DemandeVerrouilleException e) {
            log.warn("Tentative de remplacement sur demande verrouillée: {}", id);
            return ResponseEntity.status(403).build();
        } catch (BusinessException e) {
            log.error("Erreur métier lors du remplacement: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
