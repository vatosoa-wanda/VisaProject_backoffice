package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.model.Nationalite;
import com.visa.backoffice.model.DemandePiece;
import com.visa.backoffice.model.Piece;
import com.visa.backoffice.model.StatutDemande;
import com.visa.backoffice.model.TypeDemande;
import com.visa.backoffice.model.TypePiece;
import com.visa.backoffice.repository.DemandeRepository;
import com.visa.backoffice.repository.DemandePieceRepository;
import com.visa.backoffice.repository.DemandeurRepository;
import com.visa.backoffice.repository.HistoriqueStatutRepository;
import com.visa.backoffice.repository.NationaliteRepository;
import com.visa.backoffice.repository.PieceRepository;
import com.visa.backoffice.repository.StatutDemandeRepository;
import com.visa.backoffice.repository.TypeDemandeRepository;
import com.visa.backoffice.repository.TypePieceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DemandeServiceScanTest {

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private DemandeurRepository demandeurRepository;

    @Autowired
    private NationaliteRepository nationaliteRepository;

    @Autowired
    private TypeDemandeRepository typeDemandeRepository;

    @Autowired
    private StatutDemandeRepository statutDemandeRepository;

    @Autowired
    private HistoriqueStatutRepository historiqueStatutRepository;

    @Autowired
    private DemandePieceRepository demandePieceRepository;

    @Autowired
    private TypePieceRepository typePieceRepository;

    @Autowired
    private PieceRepository pieceRepository;

    @Test
    void terminerScan_statutPasseEnScanTermine_etHistoriqueAjoute() {
        StatutDemande statutCree = statutDemandeRepository.save(StatutDemande.builder().libelle("CREE").build());
        StatutDemande statutScanTermine = statutDemandeRepository.save(StatutDemande.builder().libelle("SCAN_TERMINE").build());
        TypeDemande typeDemande = typeDemandeRepository.save(TypeDemande.builder().libelle("NOUVELLE").build());
        Nationalite nat = nationaliteRepository.save(Nationalite.builder().libelle("Malagasy").build());

        Demandeur demandeur = demandeurRepository.save(Demandeur.builder()
            .nom("Test")
            .prenom("User")
            .dateNaissance(LocalDate.of(1990, 1, 1))
            .nationalite(nat)
            .adresseMadagascar("Adresse")
            .telephone("0340000000")
            .build());

        Demande demande = demandeRepository.save(Demande.builder()
            .dateDemande(LocalDateTime.now())
            .demandeur(demandeur)
            .typeDemande(typeDemande)
            .statutDemande(statutCree)
            .build());

        // Scan complet requis : attendus == fournis
        TypePiece typePiece = typePieceRepository.save(TypePiece.builder().code("COMMUN").build());
        Piece piece = pieceRepository.save(Piece.builder().nom("Pièce 1").obligatoire(true).typePiece(typePiece).build());
        demandePieceRepository.save(DemandePiece.builder().demande(demande).piece(piece).fourni(false).build());

        long historiquesAvant = historiqueStatutRepository.count();

        demandeService.terminerScan(demande.getId());

        Demande reloaded = demandeRepository.findById(demande.getId()).orElseThrow();
        assertNotNull(reloaded.getStatutDemande());
        assertEquals(statutScanTermine.getLibelle(), reloaded.getStatutDemande().getLibelle());
        assertEquals(historiquesAvant + 1, historiqueStatutRepository.count());
    }

    @Test
    void terminerScan_refuseSiScanIncomplet() {
        StatutDemande statutCree = statutDemandeRepository.save(StatutDemande.builder().libelle("CREE").build());
        statutDemandeRepository.save(StatutDemande.builder().libelle("SCAN_TERMINE").build());
        TypeDemande typeDemande = typeDemandeRepository.save(TypeDemande.builder().libelle("NOUVELLE").build());
        Nationalite nat = nationaliteRepository.save(Nationalite.builder().libelle("Malagasy").build());

        Demandeur demandeur = demandeurRepository.save(Demandeur.builder()
            .nom("Test")
            .prenom("User")
            .dateNaissance(LocalDate.of(1990, 1, 1))
            .nationalite(nat)
            .adresseMadagascar("Adresse")
            .telephone("0340000000")
            .build());

        Demande demande = demandeRepository.save(Demande.builder()
            .dateDemande(LocalDateTime.now())
            .demandeur(demandeur)
            .typeDemande(typeDemande)
            .statutDemande(statutCree)
            .build());

        // 1 pièce attendue, 0 document fourni
        TypePiece typePiece = typePieceRepository.save(TypePiece.builder().code("COMMUN").build());
        Piece piece = pieceRepository.save(Piece.builder().nom("Pièce 1").obligatoire(true).typePiece(typePiece).build());
        demandePieceRepository.save(DemandePiece.builder().demande(demande).piece(piece).fourni(false).build());

        assertThrows(BusinessException.class, () -> demandeService.terminerScan(demande.getId()));
    }

    @Test
    void terminerScan_refuseSiStatutNonCree() {
        StatutDemande statutApprouvee = statutDemandeRepository.save(StatutDemande.builder().libelle("APPROUVEE").build());
        statutDemandeRepository.save(StatutDemande.builder().libelle("SCAN_TERMINE").build());
        TypeDemande typeDemande = typeDemandeRepository.save(TypeDemande.builder().libelle("NOUVELLE").build());
        Nationalite nat = nationaliteRepository.save(Nationalite.builder().libelle("Malagasy").build());

        Demandeur demandeur = demandeurRepository.save(Demandeur.builder()
            .nom("Test")
            .prenom("User")
            .dateNaissance(LocalDate.of(1990, 1, 1))
            .nationalite(nat)
            .adresseMadagascar("Adresse")
            .telephone("0340000000")
            .build());

        Demande demande = demandeRepository.save(Demande.builder()
            .dateDemande(LocalDateTime.now())
            .demandeur(demandeur)
            .typeDemande(typeDemande)
            .statutDemande(statutApprouvee)
            .build());

        assertThrows(BusinessException.class, () -> demandeService.terminerScan(demande.getId()));
    }
}
