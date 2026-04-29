// package com.visa.backoffice.service;

// import com.visa.backoffice.dto.*;
// import com.visa.backoffice.exception.BusinessException;
// import com.visa.backoffice.mapper.DemandeMapper;
// import com.visa.backoffice.model.*;
// import com.visa.backoffice.repository.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.Collections;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyList;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class DemandeServiceTest {

//     @Mock private DemandeRepository demandeRepository;
//     @Mock private HistoriqueStatutRepository historiqueStatutRepository;
//     @Mock private DemandePieceRepository demandePieceRepository;
//     @Mock private TypeVisaRepository typeVisaRepository;
//     @Mock private TypeDemandeRepository typeDemandeRepository;
//     @Mock private StatutDemandeRepository statutDemandeRepository;
//     @Mock private PieceRepository pieceRepository;
//     @Mock private VisaTransformableService visaTransformableService;
//     @Mock private DemandeurService demandeurService;
//     @Mock private DemandeurRepository demandeurRepository;
//     @Mock private PasseportService passeportService;
//     @Mock private DemandeMapper demandeMapper;
//     @Mock private PieceService pieceService;
//     @Mock private VisaService visaService;
//     @Mock private CarteResidentService carteResidentService;

//     private DemandeService demandeService;

//     @BeforeEach
//     void setUp() {
//         demandeService = new DemandeService(
//                 demandeRepository,
//                 historiqueStatutRepository,
//                 demandePieceRepository,
//                 typeVisaRepository,
//                 typeDemandeRepository,
//                 statutDemandeRepository,
//                 pieceRepository,
//                 visaTransformableService,
//                 demandeurService,
//                 demandeurRepository,
//                 passeportService,
//                 demandeMapper,
//                 pieceService,
//                 visaService,
//                 carteResidentService
//         );
//     }

//     @Test
//     void rechercherDemandesApprouvees_throwWhenNoCriteria() {
//         assertThrows(BusinessException.class, () -> demandeService.rechercherDemandesApprouvees(DemandeRechercheDTO.builder().build()));
//     }

//     @Test
//     void rechercherDemandesApprouvees_returnsMatchingRows() {
//         Demande demande = buildApprovedNouvelleDemande(1L, "Dupont", "Paul", "P123", "VISA-1");
//         when(demandeRepository.findByStatutDemandeLibelleAndTypeDemandeLibelle("APPROUVEE", "NOUVELLE"))
//                 .thenReturn(List.of(demande));

//         List<DemandeResumeeDTO> result = demandeService.rechercherDemandesApprouvees(
//                 DemandeRechercheDTO.builder().nom("Dup").build());

//         assertEquals(1, result.size());
//         assertEquals(1L, result.get(0).getId());
//         assertEquals("Dupont", result.get(0).getDemandeurNom());
//     }

//     @Test
//     void creerDuplicata_createsNewDuplicataAndCarte() {
//         Demande origine = buildApprovedNouvelleDemande(1L, "Dupont", "Paul", "P123", "VISA-1");
//         TypeVisa typeVisa = origine.getTypeVisa();
//         TypeDemande typeDuplicata = TypeDemande.builder().id(2L).libelle("DUPLICATA").build();
//         StatutDemande statutCree = StatutDemande.builder().id(1L).libelle("CREE").build();
//         Piece pieceCommun = Piece.builder().id(10L).nom("Carte identité").obligatoire(true).typePiece(TypePiece.builder().code("COMMUN").build()).build();
//         Piece pieceSpecifique = Piece.builder().id(11L).nom("Photo").obligatoire(false).typePiece(TypePiece.builder().code("TRAVAILLEUR").build()).build();

//         when(demandeRepository.findById(1L)).thenReturn(Optional.of(origine));
//         when(demandeRepository.findFirstByIdDemandeOrigineAndTypeDemandeLibelleAndStatutDemandeLibelle(1L, "DUPLICATA", "CREE"))
//                 .thenReturn(Optional.empty());
//         when(typeDemandeRepository.findByLibelle("DUPLICATA")).thenReturn(Optional.of(typeDuplicata));
//         when(statutDemandeRepository.findByLibelle("CREE")).thenReturn(Optional.of(statutCree));
//         when(pieceRepository.findByTypePieceCodeIn(anyList())).thenReturn(List.of(pieceCommun, pieceSpecifique));
//         when(demandePieceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//         when(demandeRepository.save(any(Demande.class))).thenAnswer(invocation -> {
//             Demande demande = invocation.getArgument(0);
//             if (demande.getId() == null) {
//                 demande.setId(2L);
//             }
//             return demande;
//         });
//         when(carteResidentService.creer(any(Demande.class), any(Passeport.class))).thenReturn(CarteResident.builder().id(99L).build());
//         when(demandeMapper.toResponseDTO(any(Demande.class))).thenAnswer(invocation -> {
//             Demande demande = invocation.getArgument(0);
//             return DemandeResponseDTO.builder()
//                     .id(demande.getId())
//                     .nomDemandeur(demande.getDemandeur() != null ? demande.getDemandeur().getNom() : null)
//                     .prenomDemandeur(demande.getDemandeur() != null ? demande.getDemandeur().getPrenom() : null)
//                     .numeroPasSeport(demande.getVisaTransformable() != null && demande.getVisaTransformable().getPasseport() != null ? demande.getVisaTransformable().getPasseport().getNumero() : null)
//                     .referenceVisa(demande.getVisaTransformable() != null ? demande.getVisaTransformable().getReferenceVisa() : null)
//                     .typeVisa(demande.getTypeVisa() != null ? demande.getTypeVisa().getLibelle() : null)
//                     .typeDemande(demande.getTypeDemande() != null ? demande.getTypeDemande().getLibelle() : null)
//                     .statutDemande(demande.getStatutDemande() != null ? demande.getStatutDemande().getLibelle() : null)
//                     .pieces(Collections.emptyList())
//                     .build();
//         });

//         DemandeResponseDTO result = demandeService.creerDuplicata(DuplicataCreateDTO.builder()
//                 .idDemandeOrigine(1L)
//                 .piecesFournies(List.of(10L, 11L))
//                 .build());

//         assertEquals(2L, result.getId());
//         assertEquals("DUPLICATA", result.getTypeDemande());
//         verify(carteResidentService, times(1)).creer(any(Demande.class), any(Passeport.class));
//     }

//     @Test
//     void approuverDemandeNouvelle_createsVisaAndCarte() {
//         Demande demande = buildCreatedNouvelleDemande(1L, "Dupont", "Paul", "P123", "VISA-1");
//         StatutDemande statutApprouvee = StatutDemande.builder().id(2L).libelle("APPROUVEE").build();

//         when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande));
//         when(statutDemandeRepository.findByLibelle("APPROUVEE")).thenReturn(Optional.of(statutApprouvee));
//         when(demandeRepository.save(any(Demande.class))).thenAnswer(invocation -> invocation.getArgument(0));
//         when(visaService.creer(any(Demande.class), any(Passeport.class))).thenReturn(Visa.builder().id(3L).build());
//         when(carteResidentService.creer(any(Demande.class), any(Passeport.class))).thenReturn(CarteResident.builder().id(4L).build());
//         when(historiqueStatutRepository.save(any(HistoriqueStatut.class))).thenAnswer(invocation -> invocation.getArgument(0));

//         assertDoesNotThrow(() -> demandeService.approuverDemandeNouvelle(1L, null, true));
//         verify(visaService, times(1)).creer(any(Demande.class), any(Passeport.class));
//         verify(carteResidentService, times(1)).creer(any(Demande.class), any(Passeport.class));
//     }

//     private Demande buildApprovedNouvelleDemande(Long id, String nom, String prenom, String numeroPasseport, String referenceVisa) {
//         Demandeur demandeur = Demandeur.builder().id(1L).nom(nom).prenom(prenom).build();
//         Passeport passeport = Passeport.builder().id(1L).numero(numeroPasseport).dateDelivrance(LocalDate.now().minusYears(1)).dateExpiration(LocalDate.now().plusYears(4)).build();
//         VisaTransformable visaTransformable = VisaTransformable.builder().id(1L).referenceVisa(referenceVisa).dateEntree(LocalDate.now().minusDays(10)).lieuEntree("Antananarivo").dateExpiration(LocalDate.now().plusYears(1)).passeport(passeport).demandeur(demandeur).build();
//         TypeVisa typeVisa = TypeVisa.builder().id(1L).libelle("Travailleur").build();
//         TypeDemande typeDemande = TypeDemande.builder().id(1L).libelle("NOUVELLE").build();
//         StatutDemande statutDemande = StatutDemande.builder().id(1L).libelle("APPROUVEE").build();

//         HistoriqueStatut historique = HistoriqueStatut.builder()
//                 .id(1L)
//                 .statutDemande(statutDemande)
//                 .dateChangement(LocalDateTime.now())
//                 .commentaire("Approbation")
//                 .build();

//         Demande demande = Demande.builder()
//                 .id(id)
//                 .dateDemande(LocalDateTime.now().minusDays(1))
//                 .demandeur(demandeur)
//                 .visaTransformable(visaTransformable)
//                 .typeVisa(typeVisa)
//                 .typeDemande(typeDemande)
//                 .statutDemande(statutDemande)
//                 .historiques(new HashSet<>(List.of(historique)))
//                 .build();
//         historique.setDemande(demande);
//         return demande;
//     }

//     private Demande buildCreatedNouvelleDemande(Long id, String nom, String prenom, String numeroPasseport, String referenceVisa) {
//         Demande demande = buildApprovedNouvelleDemande(id, nom, prenom, numeroPasseport, referenceVisa);
//         demande.setStatutDemande(StatutDemande.builder().id(3L).libelle("CREE").build());
//         demande.setHistoriques(new HashSet<>());
//         return demande;
//     }
// }
