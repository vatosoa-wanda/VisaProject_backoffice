package com.visa.backoffice.service;

import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.DemandeurDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.dto.DemandeRechercheDTO;
import com.visa.backoffice.dto.DemandeResumeeDTO;
import com.visa.backoffice.dto.DuplicataCreateDTO;
import com.visa.backoffice.dto.PasseportDTO;
import com.visa.backoffice.dto.PieceDTO;
import com.visa.backoffice.dto.TransfertCreateDTO;
import com.visa.backoffice.dto.VisaTransformableDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.exception.DemandeVerrouilleException;
import com.visa.backoffice.exception.ResourceNotFoundException;
import com.visa.backoffice.mapper.DemandeMapper;
import com.visa.backoffice.model.*;
import com.visa.backoffice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final DemandePieceRepository demandePieceRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final PieceRepository pieceRepository;
    private final VisaTransformableService visaTransformableService;
    private final DemandeurService demandeurService;
    private final DemandeurRepository demandeurRepository;
    private final PasseportService passeportService;
    private final VisaService visaService;
    private final CarteResidentService carteResidentService;
    private final DemandeMapper demandeMapper;
    private final PieceService pieceService;
    private final DocumentRepository documentRepository;

    public DemandeService(DemandeRepository demandeRepository,
                         HistoriqueStatutRepository historiqueStatutRepository,
                         DemandePieceRepository demandePieceRepository,
                         TypeVisaRepository typeVisaRepository,
                         TypeDemandeRepository typeDemandeRepository,
                         StatutDemandeRepository statutDemandeRepository,
                         PieceRepository pieceRepository,
                         VisaTransformableService visaTransformableService,
                         DemandeurService demandeurService,
                         DemandeurRepository demandeurRepository,
                         PasseportService passeportService,
                         DemandeMapper demandeMapper,
                         PieceService pieceService,
                         VisaService visaService,
                         CarteResidentService carteResidentService,
                         DocumentRepository documentRepository) {
        this.demandeRepository = demandeRepository;
        this.historiqueStatutRepository = historiqueStatutRepository;
        this.demandePieceRepository = demandePieceRepository;
        this.typeVisaRepository = typeVisaRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.pieceRepository = pieceRepository;
        this.visaTransformableService = visaTransformableService;
        this.demandeurService = demandeurService;
        this.demandeurRepository = demandeurRepository;
        this.passeportService = passeportService;
        this.visaService = visaService;
        this.demandeMapper = demandeMapper;
        this.pieceService = pieceService;
        this.carteResidentService = carteResidentService;
        this.documentRepository = documentRepository;
    }

    /**
     * Crée une nouvelle demande de visa complète.
     *
     * Étapes internes (dans cet ordre) :
     *   1. Créer ou récupérer le Demandeur (Dev1)
     *   2. Créer le Passeport lié (Dev1)
     *   3. Créer le VisaTransformable
     *   4. Résoudre TypeVisa par id
     *   5. Forcer TypeDemande = "NOUVELLE"
     *   6. Forcer StatutDemande = "CREE"
     *   7. Construire et sauvegarder la Demande
     *   8. Créer l'historique de statut initial
     *   9. Lier les pièces à la demande
     *  10. Retourner le DTO de réponse
     *
     * @param dto   formulaire complet soumis par l'utilisateur
     * @return      DemandeResponseDTO avec id, statut, pièces
     * @throws BusinessException si règle métier violée
     * @throws ResourceNotFoundException si TypeVisa introuvable
     */
    public DemandeResponseDTO creerDemande(DemandeCreateDTO dto) {

        // ÉTAPE 0 — Vérifier si le demandeur existe déjà
        boolean demandeurExistait = demandeurRepository.findByNomAndDateNaissance(
                dto.getDemandeurDTO().getNom(),
                dto.getDemandeurDTO().getDateNaissance()
        ).isPresent();

        // ÉTAPE 1 — Demandeur (Dev1)
        Demandeur demandeur = demandeurService.creerOuRecuperer(dto.getDemandeurDTO());

        // ÉTAPE 2 — Passeport (Dev1)
        Passeport passeport = passeportService.creer(dto.getPasseportDTO(), demandeur);

        // ÉTAPE 3 — VisaTransformable
        VisaTransformable visa = visaTransformableService.creer(dto.getVisaDTO(), demandeur, passeport);

        // ÉTAPE 4 — TypeVisa
        TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
                .orElseThrow(() -> new ResourceNotFoundException("TypeVisa introuvable : id=" + dto.getIdTypeVisa()));

        // ÉTAPE 5 — TypeDemande forcé à NOUVELLE
        TypeDemande typeDemande = typeDemandeRepository.findByLibelle("NOUVELLE")
                .orElseThrow(() -> new ResourceNotFoundException("TypeDemande NOUVELLE introuvable en base"));

        // ÉTAPE 6 — StatutDemande forcé à CREE
        StatutDemande statutCree = statutDemandeRepository.findByLibelle("CREE")
                .orElseThrow(() -> new ResourceNotFoundException("StatutDemande CREE introuvable en base"));

        // ÉTAPE 7 — Construire et sauvegarder la Demande
        Demande demande = new Demande();
        demande.setDateDemande(LocalDateTime.now());
        demande.setDemandeur(demandeur);
        demande.setVisaTransformable(visa);
        demande.setTypeVisa(typeVisa);
        demande.setTypeDemande(typeDemande);
        demande.setStatutDemande(statutCree);
        // demande.setDemandeurExistant(demandeurExistait);
        demande = demandeRepository.save(demande);

        // ÉTAPE 8 — Historique statut initial (RG-07 : obligatoire)
        creerHistoriqueStatut(demande, statutCree, "Création de la demande");

        // ÉTAPE 9 — Lier les pièces
        lierPiecesADemande(demande, dto.getPiecesFournies(), typeVisa);

        // ÉTAPE 10 — Recharger avec pièces et retourner DTO
        Demande demandeFinal = demandeRepository.findById(demande.getId()).orElseThrow();
        return demandeMapper.toResponseDTO(demandeFinal);
    }

    /**
     * Crée une entrée dans l'historique des statuts.
     * Appelée à chaque changement de statut (y compris la création).
     *
     * @param demande       la demande concernée
     * @param statut        le nouveau statut
     * @param commentaire   motif du changement (ex: "Création de la demande")
     */
    private void creerHistoriqueStatut(Demande demande, StatutDemande statut, String commentaire) {
        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande);
        historique.setStatutDemande(statut);
        historique.setDateChangement(LocalDateTime.now());
        historique.setCommentaire(commentaire);
        historiqueStatutRepository.save(historique);
    }

    /**
     * Lie les pièces (communes + spécifiques) à une demande.
     * Vérifie que toutes les pièces obligatoires sont fournies.
     *
     * Règles :
     *   RG-08 : lier COMMUN + spécifiques selon typeVisa
     *   RG-09 : pièce obligatoire non fournie → BusinessException
     *
     * @param demande           la demande à lier
     * @param piecesFournies    ids des pièces cochées dans le formulaire
     * @param typeVisa          type de visa sélectionné
     * @throws BusinessException si pièce obligatoire manquante
     */
    private void lierPiecesADemande(Demande demande, List<Long> piecesFournies, TypeVisa typeVisa) {
        List<String> codes = List.of("COMMUN", typeVisa.getLibelle().toUpperCase());
        List<Piece> toutes = pieceRepository.findByTypePieceCodeIn(codes);

        for (Piece piece : toutes) {
            boolean fourni = piecesFournies != null && piecesFournies.contains(piece.getId());

            // RG-09 : contrôle pièce obligatoire
            if (Boolean.TRUE.equals(piece.getObligatoire()) && !fourni) {
                throw new BusinessException("Pièce obligatoire non fournie : " + piece.getNom());
            }

            DemandePiece dp = new DemandePiece();
            dp.setDemande(demande);
            dp.setPiece(piece);
            dp.setFourni(fourni);
            demandePieceRepository.save(dp);
        }
    }

    /**
     * Récupère une demande par son identifiant.
     *
     * @param id    identifiant de la demande
     * @return      DemandeResponseDTO
     * @throws ResourceNotFoundException si demande absente
     */
    public DemandeResponseDTO getDemande(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));
        return demandeMapper.toResponseDTO(demande);
    }

    /**
     * Retourne les pièces à afficher pour un type visa donné (AJAX).
     * Retourne uniquement les pièces SPÉCIFIQUES (pas les communes,
     * qui sont déjà affichées statiquement dans le formulaire).
     *
     * @param idTypeVisa    id du type visa sélectionné dans le select
     * @return              liste de PieceDTO spécifiques
     */
    public List<PieceDTO> getPiecesFormulaire(Long idTypeVisa) {
        TypeVisa typeVisa = typeVisaRepository.findById(idTypeVisa)
                .orElseThrow(() -> new ResourceNotFoundException("TypeVisa introuvable : id=" + idTypeVisa));
        return pieceService.getPiecesParTypeVisa(typeVisa.getLibelle());
    }

    /**
     * Récupère toutes les demandes.
     *
     * @return  liste de toutes les DemandeResponseDTO
     */
    public List<DemandeResponseDTO> getToutesDemandes() {
        return demandeRepository.findAll()
                .stream()
                .map(demandeMapper::toResponseDTO)
                .toList();
    }

    /**
     * Supprime une demande (et ses dépendances en cascade).
     *
     * @param id  id de la demande
     * @throws ResourceNotFoundException si demande absente
     */
    public void supprimerDemande(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

        // Supprimer les historiques
        historiqueStatutRepository.deleteAll(demande.getHistoriques());

        // Supprimer les pièces
        demandePieceRepository.deleteAll(demande.getDemandePieces());

        // Supprimer la demande
        demandeRepository.delete(demande);
    }

    public DemandeCreateDTO getDemandePourModification(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

        if (demande.getStatutDemande() != null
            && "SCAN_TERMINE".equalsIgnoreCase(demande.getStatutDemande().getLibelle())) {
            throw new DemandeVerrouilleException("Dossier verrouillé : impossible de modifier une demande au statut SCAN_TERMINE");
        }

        if (demande.getDemandeur() == null || demande.getVisaTransformable() == null || demande.getVisaTransformable().getPasseport() == null) {
            throw new BusinessException("La demande est incomplète et ne peut pas être modifiée via le formulaire.");
        }

        Demandeur demandeur = demande.getDemandeur();
        Passeport passeport = demande.getVisaTransformable().getPasseport();
        VisaTransformable visa = demande.getVisaTransformable();

        DemandeurDTO demandeurDTO = DemandeurDTO.builder()
                .nom(demandeur.getNom())
                .prenom(demandeur.getPrenom())
                .nomJeuneFille(demandeur.getNomJeuneFille())
                .dateNaissance(demandeur.getDateNaissance())
                .lieuNaissance(demandeur.getLieuNaissance())
                .idSituationFamiliale(demandeur.getSituationFamiliale() != null ? demandeur.getSituationFamiliale().getId() : null)
                .idNationalite(demandeur.getNationalite() != null ? demandeur.getNationalite().getId() : null)
                .adresseMadagascar(demandeur.getAdresseMadagascar())
                .telephone(demandeur.getTelephone())
                .email(demandeur.getEmail())
                .build();

        PasseportDTO passeportDTO = PasseportDTO.builder()
                .numero(passeport.getNumero())
                .dateDelivrance(passeport.getDateDelivrance())
                .dateExpiration(passeport.getDateExpiration())
                .build();

        VisaTransformableDTO visaDTO = VisaTransformableDTO.builder()
                .referenceVisa(visa.getReferenceVisa())
                .dateEntree(visa.getDateEntree())
                .lieuEntree(visa.getLieuEntree())
                .dateExpiration(visa.getDateExpiration())
                .build();

        List<Long> piecesFournies = demande.getDemandePieces() == null ? List.of() :
                demande.getDemandePieces().stream()
                        .filter(dp -> Boolean.TRUE.equals(dp.getFourni()))
                        .map(dp -> dp.getPiece() != null ? dp.getPiece().getId() : null)
                        .filter(Objects::nonNull)
                        .toList();

        return DemandeCreateDTO.builder()
                .demandeurDTO(demandeurDTO)
                .passeportDTO(passeportDTO)
                .visaDTO(visaDTO)
                .idTypeVisa(demande.getTypeVisa() != null ? demande.getTypeVisa().getId() : null)
                .piecesFournies(piecesFournies)
                .build();
    }

    public DemandeResponseDTO modifierDemande(Long id, DemandeCreateDTO dto) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

        if (demande.getStatutDemande() != null
            && "SCAN_TERMINE".equalsIgnoreCase(demande.getStatutDemande().getLibelle())) {
            throw new DemandeVerrouilleException("Dossier verrouillé : impossible de modifier une demande au statut SCAN_TERMINE");
        }

        if (demande.getDemandeur() == null || demande.getVisaTransformable() == null || demande.getVisaTransformable().getPasseport() == null) {
            throw new BusinessException("La demande est incomplète et ne peut pas être modifiée via le formulaire.");
        }

        // TypeVisa
        TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
                .orElseThrow(() -> new ResourceNotFoundException("TypeVisa introuvable : id=" + dto.getIdTypeVisa()));
        demande.setTypeVisa(typeVisa);

        // Demandeur / Passeport / VisaTransformable
        demandeurService.modifier(demande.getDemandeur(), dto.getDemandeurDTO());
        passeportService.modifier(demande.getVisaTransformable().getPasseport(), dto.getPasseportDTO());
        visaTransformableService.modifier(demande.getVisaTransformable(), dto.getVisaDTO());

        demandeRepository.save(demande);

        // Pièces (recalculées sur le typeVisa courant)
        demandePieceRepository.deleteAll(demande.getDemandePieces());
        lierPiecesADemande(demande, dto.getPiecesFournies(), typeVisa);

        return demandeMapper.toResponseDTO(demande);
    }

    /**
     * Verrouille la demande après scan : statut → SCAN_TERMINE + historisation.
     * Règle : uniquement autorisé si statut courant = CREE.
     */
    public void terminerScan(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + demandeId));

        String statutCourant = demande.getStatutDemande() != null ? demande.getStatutDemande().getLibelle() : null;
        if (statutCourant == null || !"CREE".equalsIgnoreCase(statutCourant)) {
            throw new BusinessException("Validation impossible : la demande doit être au statut CREE (statut actuel: " + statutCourant + ")");
        }

        long nombreDocumentsFournis = documentRepository.countByDemandeId(demandeId);
        long nombreDocumentsAttendus = demandePieceRepository.countByDemandeId(demandeId);
        if (nombreDocumentsFournis != nombreDocumentsAttendus) {
            throw new BusinessException("Impossible de terminer le scan : " + nombreDocumentsFournis + "/" + nombreDocumentsAttendus + " pièce(s) scannée(s)");
        }

        StatutDemande statutScanTermine = statutDemandeRepository.findByLibelle("SCAN_TERMINE")
            .orElseThrow(() -> new ResourceNotFoundException("StatutDemande SCAN_TERMINE introuvable en base"));

        demande.setStatutDemande(statutScanTermine);
        demandeRepository.save(demande);
        creerHistoriqueStatut(demande, statutScanTermine, "Scan terminé (verrouillage) - demande id=" + demandeId);
    }

    /**
     * Approuver une demande NOUVELLE
     * RG-01 : crée automatiquement visa + carte_resident après approbation
     *
     * @param idDemande The id of the demande to approve
     * @param commentaire Optional approval comment
     * @param automatique If true, historique will say "Approbation automatique"
     * @throws BusinessException if statut != CREE or typeDemande != NOUVELLE
     */
    public void approuverDemandeNouvelle(Long idDemande, String commentaire, boolean automatique) {
        Demande demande = demandeRepository.findById(idDemande)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + idDemande));

        if (demande.getTypeDemande() == null || !"NOUVELLE".equalsIgnoreCase(demande.getTypeDemande().getLibelle())) {
            throw new BusinessException("Seules les demandes de type NOUVELLE peuvent être approuvées ici");
        }
        if (demande.getStatutDemande() == null || !"CREE".equalsIgnoreCase(demande.getStatutDemande().getLibelle())) {
            throw new BusinessException("La demande doit être au statut CREE avant approbation");
        }

        StatutDemande statutApprouvee = statutDemandeRepository.findByLibelle("APPROUVEE")
                .orElseThrow(() -> new ResourceNotFoundException("StatutDemande APPROUVEE introuvable en base"));

        demande.setStatutDemande(statutApprouvee);
        demandeRepository.save(demande);

        String motif = (automatique ? "Approbation automatique" : "Approbation manuelle");
        if (commentaire != null && !commentaire.isBlank()) {
            motif = motif + " - " + commentaire;
        }
        creerHistoriqueStatut(demande, statutApprouvee, motif);

        Passeport passeport = demande.getVisaTransformable() != null ? demande.getVisaTransformable().getPasseport() : null;
        if (passeport == null) {
            throw new BusinessException("Passeport introuvable sur la demande");
        }

        visaService.creer(demande, passeport);
        carteResidentService.creer(demande, passeport);
    }

    /**
     * Rechercher les demandes APPROUVEE type NOUVELLE
     * Utilisé par Dev2 pour DUPLICATA avec antécédent
     *
     * @param criteresRecherche DemandeRechercheDTO (min 1 critère requis)
     * @return List<DemandeResumeeDTO>
     * @throws BusinessException if no criteria provided
     */
    public List<DemandeResumeeDTO> rechercherDemandesApprouvees(DemandeRechercheDTO criteresRecherche) {
        if ((criteresRecherche.getNom() == null || criteresRecherche.getNom().isBlank())
                && (criteresRecherche.getNumeroPasSeport() == null || criteresRecherche.getNumeroPasSeport().isBlank())
                && (criteresRecherche.getReferenceVisa() == null || criteresRecherche.getReferenceVisa().isBlank())) {
            throw new BusinessException("Au moins un critère de recherche est requis");
        }

        return demandeRepository.findByStatutDemandeLibelleAndTypeDemandeLibelle("APPROUVEE", "NOUVELLE").stream()
                .filter(d -> d.getStatutDemande() != null && "APPROUVEE".equalsIgnoreCase(d.getStatutDemande().getLibelle()))
                .filter(d -> d.getTypeDemande() != null && "NOUVELLE".equalsIgnoreCase(d.getTypeDemande().getLibelle()))
                .filter(d -> {
                    boolean match = true;
                    if (criteresRecherche.getNom() != null && !criteresRecherche.getNom().isBlank()) {
                        match = match && d.getDemandeur() != null && d.getDemandeur().getNom() != null && d.getDemandeur().getNom().toLowerCase().contains(criteresRecherche.getNom().toLowerCase());
                    }
                    if (criteresRecherche.getNumeroPasSeport() != null && !criteresRecherche.getNumeroPasSeport().isBlank()) {
                        match = match && d.getVisaTransformable() != null && d.getVisaTransformable().getPasseport() != null
                                && criteresRecherche.getNumeroPasSeport().equalsIgnoreCase(d.getVisaTransformable().getPasseport().getNumero());
                    }
                    if (criteresRecherche.getReferenceVisa() != null && !criteresRecherche.getReferenceVisa().isBlank()) {
                        match = match && d.getVisaTransformable() != null && d.getVisaTransformable().getReferenceVisa() != null
                                && criteresRecherche.getReferenceVisa().equalsIgnoreCase(d.getVisaTransformable().getReferenceVisa());
                    }
                    return match;
                })
                .map(d -> {
                    java.time.LocalDateTime dateApproval = d.getHistoriques().stream()
                            .filter(h -> h.getStatutDemande() != null && "APPROUVEE".equalsIgnoreCase(h.getStatutDemande().getLibelle()))
                            .map(h -> h.getDateChangement())
                            .max(java.time.LocalDateTime::compareTo)
                            .orElse(null);

                    return DemandeResumeeDTO.builder()
                            .id(d.getId())
                            .demandeurNom(d.getDemandeur() != null ? d.getDemandeur().getNom() : null)
                            .demandeurPrenom(d.getDemandeur() != null ? d.getDemandeur().getPrenom() : null)
                            .numeroPasSeport(d.getVisaTransformable() != null && d.getVisaTransformable().getPasseport() != null ? d.getVisaTransformable().getPasseport().getNumero() : null)
                            .referenceVisa(d.getVisaTransformable() != null ? d.getVisaTransformable().getReferenceVisa() : null)
                            .dateApproval(dateApproval)
                            .build();
                })
                .toList();
    }

    /**
     * Créer une demande DUPLICATA
     * RG-02, RG-06 : crée une demande DUPLICATA avec nouvelle carte_resident
     *
     * @param dto DuplicataCreateDTO contenant idDemandeOrigine + pièces
     * @return Created demande DUPLICATA (statut=CREE)
     * @throws BusinessException if origine invalid or doublon exists
     */
    public DemandeResponseDTO creerDuplicata(DuplicataCreateDTO dto) {
        com.visa.backoffice.model.Demande origine = demandeRepository.findById(dto.getIdDemandeOrigine())
                .orElseThrow(() -> new ResourceNotFoundException("Demande origine introuvable : id=" + dto.getIdDemandeOrigine()));

        if (origine.getTypeDemande() == null || !"NOUVELLE".equalsIgnoreCase(origine.getTypeDemande().getLibelle())) {
            throw new BusinessException("La demande origine doit être de type NOUVELLE");
        }
        if (origine.getStatutDemande() == null || !"APPROUVEE".equalsIgnoreCase(origine.getStatutDemande().getLibelle())) {
            throw new BusinessException("La demande origine doit être APPROUVEE");
        }

        // Vérifier doublon : DUPLICATA en statut CREE existant pour même origine
        if (demandeRepository.findFirstByIdDemandeOrigineAndTypeDemandeLibelleAndStatutDemandeLibelle(
            origine.getId(), "DUPLICATA", "CREE").isPresent()) {
            throw new BusinessException("Un duplicata en cours existe déjà pour cette demande origine");
        }

        TypeDemande typeDuplicata = typeDemandeRepository.findByLibelle("DUPLICATA")
                .orElseThrow(() -> new ResourceNotFoundException("TypeDemande DUPLICATA introuvable en base"));
        StatutDemande statutCree = statutDemandeRepository.findByLibelle("CREE")
                .orElseThrow(() -> new ResourceNotFoundException("StatutDemande CREE introuvable en base"));

        com.visa.backoffice.model.Demande duplicata = new com.visa.backoffice.model.Demande();
        duplicata.setDateDemande(java.time.LocalDateTime.now());
        duplicata.setDemandeur(origine.getDemandeur());
        duplicata.setVisaTransformable(origine.getVisaTransformable());
        duplicata.setTypeVisa(origine.getTypeVisa());
        duplicata.setTypeDemande(typeDuplicata);
        duplicata.setStatutDemande(statutCree);
        duplicata.setIdDemandeOrigine(origine.getId());

        duplicata = demandeRepository.save(duplicata);

        creerHistoriqueStatut(duplicata, statutCree, "Création duplicata (origine id=" + origine.getId() + ")");

        // Lier pièces
        lierPiecesADemande(duplicata, dto.getPiecesFournies(), origine.getTypeVisa());

        // Créer la nouvelle carte résident liée au duplicata
        com.visa.backoffice.model.Passeport passeport = origine.getVisaTransformable() != null ? origine.getVisaTransformable().getPasseport() : null;
        if (passeport == null) {
            throw new BusinessException("Passeport introuvable sur la demande origine");
        }
        carteResidentService.creerPourDuplicata(duplicata, passeport);

        com.visa.backoffice.model.Demande demandeFinal = demandeRepository.findById(duplicata.getId()).orElseThrow();
        return demandeMapper.toResponseDTO(demandeFinal);
    }

    /**
     * Créer une demande TRANSFERT
     * RG-03, RG-07 : crée une demande TRANSFERT avec nouveau visa, ancien visa désactivé
     *
     * @param dto TransfertCreateDTO contenant idDemandeOrigine + nouveauPasseport + pièces
     * @return Created demande TRANSFERT (statut=CREE)
     * @throws BusinessException if origine invalid, doublon, or same passeport number
     */
    public DemandeResponseDTO creerTransfert(TransfertCreateDTO dto) {
        // Dev1 implémente ici
        throw new UnsupportedOperationException("À implémenter par Dev1");
    }
}