package com.visa.backoffice.service;

import com.visa.backoffice.dto.*;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.exception.ResourceNotFoundException;
import com.visa.backoffice.mapper.DemandeMapper;
import com.visa.backoffice.model.*;
import com.visa.backoffice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private final PasseportRepository passeportRepository;
    private final VisaTransformableRepository visaTransformableRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final DemandeMapper demandeMapper;
    private final PieceService pieceService;

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
                         PasseportRepository passeportRepository,
                         VisaTransformableRepository visaTransformableRepository,
                         SituationFamilialeRepository situationFamilialeRepository,
                         DemandeMapper demandeMapper,
                         PieceService pieceService) {
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
        this.passeportRepository = passeportRepository;
        this.visaTransformableRepository = visaTransformableRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.demandeMapper = demandeMapper;
        this.pieceService = pieceService;
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
        VisaTransformable visa = visaTransformableService.creer(dto.getVisaDTO(), passeport);

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

    // ════════════════════════════════════════════════════════
    //  MODIFICATION D'UNE DEMANDE
    // ════════════════════════════════════════════════════════

    /**
     * Récupère une demande au format édition (DTO pour le formulaire de modification).
     *
     * @param id    identifiant de la demande
     * @return      DemandeUpdateDTO prêt pour être modifié
     * @throws ResourceNotFoundException si demande absente
     */
    public DemandeUpdateDTO getDemandePourEdition(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));
        return demandeMapper.toUpdateDTO(demande);
    }

    /**
     * Modifie une demande existante et toutes ses relations.
     * Applique toutes les validations existantes lors de la modification.
     *
     * Étapes internes (dans cet ordre) :
     *   1. Récupérer la demande existante
     *   2. Mettre à jour le demandeur (champs modifiables seulement)
     *   3. Mettre à jour le passeport
     *   4. Mettre à jour le VisaTransformable
     *   5. Mettre à jour le TypeVisa
     *   6. Recréer les liens DemandePiece (supprimer + ajouter)
     *   7. Sauvegarder la demande
     *   8. Retourner le DTO de réponse mis à jour
     *
     * Règles appliquées :
     *   RG-11 : reconstruction des relations demande_piece
     *   RG-12 : dateDemande, idDemandeur, typeDemande immutables
     *   RG-13 : validations existantes appliquées
     *
     * @param id    identifiant de la demande à modifier
     * @param dto   nouvelles données (formulaire soumis)
     * @return      DemandeResponseDTO avec modifications appliquées
     * @throws BusinessException si règle métier violée
     * @throws ResourceNotFoundException si demande absente
     */
    public DemandeResponseDTO updateDemande(Long id, DemandeUpdateDTO dto) {

        // ÉTAPE 1 — Récupérer la demande existante
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

        // ÉTAPE 2 — Mettre à jour le Demandeur (champs modifiables uniquement)
        // ⚠️ NE PAS MODIFIER : nom, dateNaissance, nationalité
        Demandeur demandeur = demande.getDemandeur();
        if (demandeur != null && dto.getDemandeurDTO() != null) {
            demandeur.setPrenom(dto.getDemandeurDTO().getPrenom());
            demandeur.setNomJeuneFille(dto.getDemandeurDTO().getNomJeuneFille());
            demandeur.setLieuNaissance(dto.getDemandeurDTO().getLieuNaissance());
            demandeur.setAdresseMadagascar(dto.getDemandeurDTO().getAdresseMadagascar());
            demandeur.setTelephone(dto.getDemandeurDTO().getTelephone());
            demandeur.setEmail(dto.getDemandeurDTO().getEmail());

            if (dto.getDemandeurDTO().getIdSituationFamiliale() != null) {
                SituationFamiliale sf = situationFamilialeRepository
                    .findById(dto.getDemandeurDTO().getIdSituationFamiliale())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Situation familiale introuvable : id=" + dto.getDemandeurDTO().getIdSituationFamiliale()));
                demandeur.setSituationFamiliale(sf);
            }
        }

        // ÉTAPE 3 — Mettre à jour le Passeport
        VisaTransformable visa = demande.getVisaTransformable();
        if (visa != null) {
            Passeport passeport = visa.getPasseport();
            if (passeport != null && dto.getPasseportDTO() != null) {
                String ancienNumero = passeport.getNumero();
                String nouveauNumero = dto.getPasseportDTO().getNumero();

                // Vérifier unicité du nouveau numéro (si modifié)
                if (!ancienNumero.equals(nouveauNumero)) {
                    Optional<Passeport> existant = passeportRepository.findByNumero(nouveauNumero);
                    if (existant.isPresent()) {
                        throw new BusinessException("Le numéro de passeport '" + nouveauNumero + "' est déjà utilisé.");
                    }
                }

                // Vérifier cohérence des dates
                if (!dto.getPasseportDTO().getDateExpiration()
                    .isAfter(dto.getPasseportDTO().getDateDelivrance())) {
                    throw new BusinessException(
                        "La date d'expiration doit être postérieure à la date de délivrance.");
                }

                passeport.setNumero(nouveauNumero);
                passeport.setDateDelivrance(dto.getPasseportDTO().getDateDelivrance());
                passeport.setDateExpiration(dto.getPasseportDTO().getDateExpiration());
            }

            // ÉTAPE 4 — Mettre à jour le VisaTransformable
            if (dto.getVisaDTO() != null) {
                String ancienneRef = visa.getReferenceVisa();
                String nouvelleRef = dto.getVisaDTO().getReferenceVisa();

                // Vérifier unicité de la nouvelle référence (si modifiée)
                if (!ancienneRef.equals(nouvelleRef)) {
                    Optional<VisaTransformable> existant = visaTransformableRepository.findByReferenceVisa(nouvelleRef);
                    if (existant.isPresent()) {
                        throw new BusinessException("La référence visa '" + nouvelleRef + "' est déjà utilisée.");
                    }
                }

                // Vérifier cohérence des dates
                if (!dto.getVisaDTO().getDateExpiration().isAfter(dto.getVisaDTO().getDateEntree())) {
                    throw new BusinessException(
                        "La date d'expiration du visa doit être postérieure à la date d'entrée.");
                }

                visa.setReferenceVisa(nouvelleRef);
                visa.setDateEntree(dto.getVisaDTO().getDateEntree());
                visa.setLieuEntree(dto.getVisaDTO().getLieuEntree());
                visa.setDateExpiration(dto.getVisaDTO().getDateExpiration());
            }
        }

        // ÉTAPE 5 — Mettre à jour le TypeVisa
        TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
                .orElseThrow(() -> new ResourceNotFoundException("TypeVisa introuvable : id=" + dto.getIdTypeVisa()));
        demande.setTypeVisa(typeVisa);

        // ÉTAPE 6 — Recréer les liens DemandePiece
        demandePieceRepository.deleteByDemandeId(id);
        lierPiecesADemande(demande, dto.getPiecesFournies(), typeVisa);

        // ÉTAPE 7 — Sauvegarder la demande
        demande = demandeRepository.save(demande);

        // ÉTAPE 8 — Recharger avec pièces et retourner DTO
        Demande demandeFinal = demandeRepository.findById(demande.getId()).orElseThrow();
        return demandeMapper.toResponseDTO(demandeFinal);
    }
}