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
    private final DemandeMapper demandeMapper;
    private final PieceService pieceService;
    private final VisaService visaService;
    private final CarteResidentService carteResidentService;

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

                         CarteResidentService carteResidentService) {
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
        this.demandeMapper = demandeMapper;
        this.pieceService = pieceService;
        this.visaService = visaService;
        this.carteResidentService = carteResidentService;
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
     * Approuver une demande NOUVELLE
     * RG-01 : crée automatiquement visa + carte_resident après approbation
     *
     * @param idDemande The id of the demande to approve
     * @param commentaire Optional approval comment
     * @param automatique If true, historique will say "Approbation automatique"
     * @throws BusinessException if statut != CREE or typeDemande != NOUVELLE
     */
    public void approuverDemandeNouvelle(Long idDemande, String commentaire, boolean automatique) {
        // Dev1 implémente ici
        // 1. Charger demande → erreur si absente
        // 2. Vérifier statut=CREE et type=NOUVELLE → BusinessException sinon
        // 3. Passer statut → APPROUVEE, sauvegarder
        // 4. Créer historique ("Approbation automatique" si automatique=true)
        // 5. visaService.creer(demande, passeport)           ← RG-01
        // 6. carteResidentService.creer(demande, passeport)  ← RG-01
        throw new UnsupportedOperationException("À implémenter par Dev1");
    }

    /**
     * Rechercher les demandes APPROUVEE type NOUVELLE
     * Utilisé pour TRANSFERT avec antécédent
     *
     * @param criteresRecherche DemandeRechercheDTO (min 1 critère requis)
     * @return List<DemandeResumeeDTO>
     * @throws BusinessException if no criteria provided
     */
    public List<DemandeResumeeDTO> rechercherDemandesApprouvees(DemandeRechercheDTO criteresRecherche) {
        // Valider qu'au moins un critère est fourni
        if (criteresRecherche == null || 
            (criteresRecherche.getNom() == null || criteresRecherche.getNom().trim().isEmpty()) && 
            (criteresRecherche.getNumeroPasSeport() == null || criteresRecherche.getNumeroPasSeport().trim().isEmpty()) && 
            (criteresRecherche.getReferenceVisa() == null || criteresRecherche.getReferenceVisa().trim().isEmpty())) {
            throw new BusinessException("Au moins un critère de recherche est requis");
        }

        // Récupérer les statuts et types requis
        StatutDemande statutApprouve = statutDemandeRepository.findByLibelle("APPROUVEE")
                .orElseThrow(() -> new ResourceNotFoundException("Statut APPROUVEE introuvable"));
        
        TypeDemande typeNouvelle = typeDemandeRepository.findByLibelle("NOUVELLE")
                .orElseThrow(() -> new ResourceNotFoundException("Type NOUVELLE introuvable"));

        // Construire la recherche dynamique
        List<Demande> demandes = demandeRepository.findAll().stream()
                .filter(d -> d.getStatutDemande() != null && d.getStatutDemande().getId().equals(statutApprouve.getId()))
                .filter(d -> d.getTypeDemande() != null && d.getTypeDemande().getId().equals(typeNouvelle.getId()))
                .filter(d -> {
                    Demandeur demandeur = d.getDemandeur();
                    if (demandeur == null) return false;
                    
                    boolean matchNom = criteresRecherche.getNom() == null || 
                        criteresRecherche.getNom().trim().isEmpty() ||
                        demandeur.getNom().toLowerCase().contains(criteresRecherche.getNom().toLowerCase());
                    
                    boolean matchPasseport = criteresRecherche.getNumeroPasSeport() == null || 
                        criteresRecherche.getNumeroPasSeport().trim().isEmpty() ||
                        (d.getVisaTransformable() != null && 
                         d.getVisaTransformable().getPasseport() != null &&
                         d.getVisaTransformable().getPasseport().getNumero().toLowerCase()
                            .contains(criteresRecherche.getNumeroPasSeport().toLowerCase()));
                    
                    boolean matchReferenceVisa = criteresRecherche.getReferenceVisa() == null || 
                        criteresRecherche.getReferenceVisa().trim().isEmpty() ||
                        (d.getVisaTransformable() != null &&
                         d.getVisaTransformable().getReferenceVisa() != null &&
                         d.getVisaTransformable().getReferenceVisa().toLowerCase()
                            .contains(criteresRecherche.getReferenceVisa().toLowerCase()));
                    
                    return matchNom && matchPasseport && matchReferenceVisa;
                })
                .limit(50) // Limiter les résultats
                .toList();

        return demandes.stream()
                .map(demandeMapper::toResumeeDTO)
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
        // Dev2 implémente ici
        throw new UnsupportedOperationException("À implémenter par Dev2");
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
        // ÉTAPE 1 — Vérifier la demande d'origine
        Demande demandeOrigine = demandeRepository.findById(dto.getIdDemandeOrigine())
                .orElseThrow(() -> new ResourceNotFoundException("Demande d'origine introuvable : id=" + dto.getIdDemandeOrigine()));

        // Vérifier statut = APPROUVEE et type = NOUVELLE
        if (demandeOrigine.getStatutDemande() == null || !demandeOrigine.getStatutDemande().getLibelle().equals("APPROUVEE")) {
            throw new BusinessException("La demande d'origine doit avoir un statut APPROUVÉE");
        }
        if (demandeOrigine.getTypeDemande() == null || !demandeOrigine.getTypeDemande().getLibelle().equals("NOUVELLE")) {
            throw new BusinessException("La demande d'origine doit être de type NOUVELLE");
        }

        // ÉTAPE 2 — Vérifier absence doublon TRANSFERT actif (statut CREE)
        boolean doublonExists = demandeRepository.findAll().stream()
                .anyMatch(d -> d.getIdDemandeOrigine() != null
                        && d.getIdDemandeOrigine().equals(dto.getIdDemandeOrigine())
                        && d.getTypeDemande() != null
                        && d.getTypeDemande().getLibelle().equals("TRANSFERT")
                        && d.getStatutDemande() != null
                        && d.getStatutDemande().getLibelle().equals("CREE"));
        if (doublonExists) {
            throw new BusinessException("Un transfert actif existe déjà pour cette demande");
        }

        // ÉTAPE 3 — Créer nouveau Passeport
        Passeport passeportAncien = demandeOrigine.getVisaTransformable().getPasseport();
        String numéroAncien = passeportAncien != null ? passeportAncien.getNumero() : null;
        String numéroNouveau = dto.getPasseportNouveau().getNumero();

        // Vérifier que le nouveau numéro est différent de l'ancien
        if (numéroAncien != null && numéroAncien.equals(numéroNouveau)) {
            throw new BusinessException("Le nouveau numéro de passeport doit être différent de l'ancien");
        }

        Passeport passeportNouveau = passeportService.creer(dto.getPasseportNouveau(), demandeOrigine.getDemandeur());

        // ÉTAPE 4 — Créer demande TRANSFERT
        TypeDemande typeTransfert = typeDemandeRepository.findByLibelle("TRANSFERT")
                .orElseThrow(() -> new ResourceNotFoundException("TypeDemande TRANSFERT introuvable en base"));

        StatutDemande statutCree = statutDemandeRepository.findByLibelle("CREE")
                .orElseThrow(() -> new ResourceNotFoundException("StatutDemande CREE introuvable en base"));

        Demande demandTransfert = new Demande();
        demandTransfert.setDateDemande(LocalDateTime.now());
        demandTransfert.setDemandeur(demandeOrigine.getDemandeur());
        demandTransfert.setVisaTransformable(demandeOrigine.getVisaTransformable());
        demandTransfert.setTypeVisa(demandeOrigine.getTypeVisa());
        demandTransfert.setTypeDemande(typeTransfert);
        demandTransfert.setStatutDemande(statutCree);
        demandTransfert.setIdDemandeOrigine(demandeOrigine.getId());
        demandTransfert = demandeRepository.save(demandTransfert);

        // ÉTAPE 5 — Désactiver l'ancien visa
        visaService.desactiver(demandeOrigine.getId());

        // ÉTAPE 6 — Créer nouveau visa avec nouveau passeport
        visaService.creer(demandTransfert, passeportNouveau);

        // ÉTAPE 7 — Créer historique statut
        creerHistoriqueStatut(demandTransfert, statutCree, "Création de la demande TRANSFERT");

        // ÉTAPE 8 — Lier les pièces
        lierPiecesADemande(demandTransfert, dto.getPiecesFournies(), demandeOrigine.getTypeVisa());

        // ÉTAPE 9 — Recharger et retourner DTO
        Demande demandTransfertFinal = demandeRepository.findById(demandTransfert.getId()).orElseThrow();
        return demandeMapper.toResponseDTO(demandTransfertFinal);
    }
}