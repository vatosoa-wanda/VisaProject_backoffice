package com.visa.backoffice.service;

import com.visa.backoffice.dto.DocumentDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.exception.DemandeVerrouilleException;
import com.visa.backoffice.exception.ResourceNotFoundException;
import com.visa.backoffice.mapper.DocumentMapper;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Document;
import com.visa.backoffice.model.Piece;
import com.visa.backoffice.repository.DocumentRepository;
import com.visa.backoffice.repository.DemandeRepository;
import com.visa.backoffice.repository.PieceRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DemandeRepository demandeRepository;
    private final PieceRepository pieceRepository;
    private final DocumentMapper documentMapper;

    @Value("${app.upload.dir:uploads/documents/}")
    private String uploadDir;

    public DocumentService(DocumentRepository documentRepository,
                          DemandeRepository demandeRepository,
                          PieceRepository pieceRepository,
                          DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.demandeRepository = demandeRepository;
        this.pieceRepository = pieceRepository;
        this.documentMapper = documentMapper;
    }

    /**
     * Crée le dossier d'upload s'il n'existe pas
     */
    @PostConstruct
    public void initializeUploadDirectory() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Dossier d'upload créé : {}", uploadDir);
            }
        } catch (IOException e) {
            log.error("Erreur lors de la création du dossier d'upload : {}", uploadDir, e);
        }
    }

    /**
     * Upload un nouveau document pour une demande
     * Si un document existe déjà pour cette pièce, il sera remplacé
     * @param demandeId ID de la demande
     * @param pieceId ID de la pièce
     * @param fichier Fichier à uploader
     * @return Le document créé ou mis à jour
     * @throws BusinessException si le statut n'est pas CREE
     */
    public DocumentDTO uploadDocument(Long demandeId, Long pieceId, MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new BusinessException("Aucun fichier fourni");
        }

        // Récupérer la demande
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + demandeId));

        // Vérifier le statut
        if (demande.getStatutDemande() == null ||
            !"CREE".equalsIgnoreCase(demande.getStatutDemande().getLibelle())) {
            throw new DemandeVerrouilleException("Document ne peut pas être uploadé - Demande verrouillée (statut: " +
                (demande.getStatutDemande() != null ? demande.getStatutDemande().getLibelle() : "NULL") + ")");
        }

        // Récupérer la pièce
        Piece piece = pieceRepository.findById(pieceId)
            .orElseThrow(() -> new ResourceNotFoundException("Pièce introuvable : id=" + pieceId));

        // Vérifier si un document existe déjà pour cette pièce et demande
        Document document = documentRepository.findByDemandeIdAndPieceId(demandeId, pieceId)
            .orElse(null);

        // Générer le nom unique
        String nomUnique = genererNomFichierUnique(demandeId, pieceId, fichier.getOriginalFilename());
        String cheminFichier = uploadDir + nomUnique;

        try {
            // Si un document existe, supprimer l'ancien fichier
            if (document != null) {
                try {
                    Path oldPath = Paths.get(document.getCheminFichier());
                    if (Files.exists(oldPath)) {
                        Files.delete(oldPath);
                        log.info("Ancien fichier supprimé : {}", document.getCheminFichier());
                    }
                } catch (IOException e) {
                    log.error("Erreur lors de la suppression de l'ancien fichier : {}", document.getCheminFichier(), e);
                }
            } else {
                // Créer un nouveau document si aucun n'existe
                document = Document.builder()
                    .demande(demande)
                    .piece(piece)
                    .build();
            }

            // Sauvegarder le fichier
            Path path = Paths.get(cheminFichier);
            Files.write(path, fichier.getBytes());
            log.info("Fichier uploadé : {}", cheminFichier);

            // Mettre à jour l'enregistrement en BDD
            document.setNomOriginal(fichier.getOriginalFilename());
            document.setCheminFichier(cheminFichier);
            document.setTailleFichier(fichier.getSize());
            document.setTypeMime(fichier.getContentType());
            document.setDateCreation(LocalDateTime.now());

            Document saved = documentRepository.save(document);
            log.info("Document enregistré/mis à jour en BDD : id={}, demande={}", saved.getId(), demandeId);

            return documentMapper.toDTO(saved);
        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde du fichier : {}", cheminFichier, e);
            throw new BusinessException("Erreur lors de l'upload du fichier : " + e.getMessage());
        }
    }

    /**
     * Supprime un document
     * @param docId ID du document à supprimer
     * @throws ResourceNotFoundException si le document n'existe pas
     * @throws DemandeVerrouilleException si la demande est verrouillée
     */
    public void supprimerDocument(Long docId) {
        // Récupérer le document
        Document document = documentRepository.findById(docId)
            .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : id=" + docId));

        // Vérifier que la demande n'est pas verrouillée
        if (document.getDemande().getStatutDemande() != null &&
            "SCAN_TERMINE".equalsIgnoreCase(document.getDemande().getStatutDemande().getLibelle())) {
            throw new DemandeVerrouilleException("Impossible de supprimer - Demande verrouillée (SCAN_TERMINE)");
        }

        // Supprimer le fichier physique
        try {
            Path path = Paths.get(document.getCheminFichier());
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Fichier supprimé : {}", document.getCheminFichier());
            }
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier : {}", document.getCheminFichier(), e);
            // On continue la suppression en BDD même si la suppression du fichier échoue
        }

        // Supprimer l'enregistrement en BDD
        documentRepository.delete(document);
        log.info("Document supprimé de la BDD : id={}", docId);
    }

    /**
     * Remplace un document existant
     * @param docId ID du document à remplacer
     * @param fichier Nouveau fichier
     * @return Le document mis à jour
     * @throws ResourceNotFoundException si le document n'existe pas
     * @throws DemandeVerrouilleException si la demande est verrouillée
     */
    public DocumentDTO remplacerDocument(Long docId, MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new BusinessException("Aucun fichier fourni");
        }

        // Récupérer le document existant
        Document document = documentRepository.findById(docId)
            .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : id=" + docId));

        // Vérifier que la demande n'est pas verrouillée
        if (document.getDemande().getStatutDemande() != null &&
            "SCAN_TERMINE".equalsIgnoreCase(document.getDemande().getStatutDemande().getLibelle())) {
            throw new DemandeVerrouilleException("Impossible de modifier - Demande verrouillée (SCAN_TERMINE)");
        }

        // Supprimer l'ancien fichier physique
        try {
            Path oldPath = Paths.get(document.getCheminFichier());
            if (Files.exists(oldPath)) {
                Files.delete(oldPath);
                log.info("Ancien fichier supprimé : {}", document.getCheminFichier());
            }
        } catch (IOException e) {
            log.error("Erreur lors de la suppression de l'ancien fichier : {}", document.getCheminFichier(), e);
        }

        // Générer nouveau nom unique
        String nomUnique = genererNomFichierUnique(
            document.getDemande().getId(),
            document.getPiece().getId(),
            fichier.getOriginalFilename()
        );
        String cheminFichierNew = uploadDir + nomUnique;

        try {
            // Sauvegarder le nouveau fichier
            Path path = Paths.get(cheminFichierNew);
            Files.write(path, fichier.getBytes());
            log.info("Nouveau fichier uploadé : {}", cheminFichierNew);

            // Mettre à jour l'enregistrement en BDD
            document.setNomOriginal(fichier.getOriginalFilename());
            document.setCheminFichier(cheminFichierNew);
            document.setTailleFichier(fichier.getSize());
            document.setTypeMime(fichier.getContentType());
            document.setDateCreation(LocalDateTime.now());

            Document updated = documentRepository.save(document);
            log.info("Document mis à jour en BDD : id={}", updated.getId());

            return documentMapper.toDTO(updated);
        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde du nouveau fichier : {}", cheminFichierNew, e);
            throw new BusinessException("Erreur lors du remplacement du fichier : " + e.getMessage());
        }
    }

    /**
     * Récupère tous les documents d'une demande
     */
    public List<DocumentDTO> getDocumentsByDemande(Long demandeId) {
        // Vérifier que la demande existe
        if (!demandeRepository.existsById(demandeId)) {
            throw new ResourceNotFoundException("Demande introuvable : id=" + demandeId);
        }

        List<Document> documents = documentRepository.findByDemandeId(demandeId);
        return documents.stream()
            .map(documentMapper::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupère un document par son ID avec vérification de propriété
     */
    public DocumentDTO getDocumentById(Long docId, Long demandeId) {
        Document document = documentRepository.findByIdAndDemandeId(docId, demandeId)
            .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : id=" + docId));
        return documentMapper.toDTO(document);
    }

    /**
     * Récupère le contenu du fichier document
     */
    @Transactional(readOnly = true)
    public byte[] getDocumentFile(Long docId) throws IOException {
        Document document = documentRepository.findById(docId)
            .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : id=" + docId));

        Path path = Paths.get(document.getCheminFichier());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Fichier introuvable : " + document.getCheminFichier());
        }

        return Files.readAllBytes(path);
    }

    /**
     * Détermine le Content-Type basé sur l'extension du fichier
     */
    public String determineContentType(String nomFichier) {
        if (nomFichier == null) {
            return "application/octet-stream";
        }

        String lowerName = nomFichier.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".txt")) {
            return "text/plain";
        }
        return "application/octet-stream";
    }

    /**
     * Génère un nom de fichier unique pour éviter les collisions
     */
    private String genererNomFichierUnique(Long demandeId, Long pieceId, String originalFilename) {
        String extension = extraireExtension(originalFilename);
        String timestamp = System.currentTimeMillis() + "";
        return demandeId + "_" + pieceId + "_" + timestamp + "." + extension;
    }

    /**
     * Extrait l'extension d'un nom de fichier
     */
    private String extraireExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
