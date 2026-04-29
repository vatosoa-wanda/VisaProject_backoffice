package com.visa.backoffice.service;

import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.model.Document;
import com.visa.backoffice.model.Nationalite;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Piece;
import com.visa.backoffice.model.StatutDemande;
import com.visa.backoffice.model.TypeDemande;
import com.visa.backoffice.model.TypePiece;
import com.visa.backoffice.model.VisaTransformable;
import com.visa.backoffice.repository.DemandeRepository;
import com.visa.backoffice.repository.DemandeurRepository;
import com.visa.backoffice.repository.DocumentRepository;
import com.visa.backoffice.repository.NationaliteRepository;
import com.visa.backoffice.repository.PasseportRepository;
import com.visa.backoffice.repository.PieceRepository;
import com.visa.backoffice.repository.StatutDemandeRepository;
import com.visa.backoffice.repository.TypeDemandeRepository;
import com.visa.backoffice.repository.TypePieceRepository;
import com.visa.backoffice.repository.VisaTransformableRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PdfServiceTest {

    @Autowired
    private PdfService pdfService;

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
    private PasseportRepository passeportRepository;

    @Autowired
    private VisaTransformableRepository visaTransformableRepository;

    @Autowired
    private TypePieceRepository typePieceRepository;

    @Autowired
    private PieceRepository pieceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void genererAttestation_retourneUnPdfNonVide() {
        StatutDemande statutCree = statutDemandeRepository.save(StatutDemande.builder().libelle("CREE").build());
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

        Passeport passeport = passeportRepository.save(Passeport.builder()
            .numero("P-123")
            .dateDelivrance(LocalDate.of(2024, 1, 1))
            .dateExpiration(LocalDate.of(2030, 1, 1))
            .demandeur(demandeur)
            .build());

        VisaTransformable visa = visaTransformableRepository.save(VisaTransformable.builder()
            .referenceVisa("VISA-REF")
            .dateEntree(LocalDate.of(2024, 2, 1))
            .dateExpiration(LocalDate.of(2026, 2, 1))
            .demandeur(demandeur)
            .passeport(passeport)
            .build());

        Demande demande = demandeRepository.save(Demande.builder()
            .dateDemande(LocalDateTime.now())
            .demandeur(demandeur)
            .typeDemande(typeDemande)
            .statutDemande(statutCree)
            .visaTransformable(visa)
            .build());

        TypePiece typePiece = typePieceRepository.save(TypePiece.builder().code("COMMUN").build());
        Piece piece = pieceRepository.save(Piece.builder().nom("Photocopie passeport").obligatoire(true).typePiece(typePiece).build());

        documentRepository.save(Document.builder()
            .demande(demande)
            .piece(piece)
            .nomOriginal("test.pdf")
            .cheminFichier("uploads/documents/fake.pdf")
            .typeMime("application/pdf")
            .tailleFichier(10L)
            .dateCreation(LocalDateTime.now())
            .build());

        byte[] pdfBytes = pdfService.genererAttestation(demande.getId());
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100);

        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 4));
        assertEquals("%PDF", header);
    }
}
