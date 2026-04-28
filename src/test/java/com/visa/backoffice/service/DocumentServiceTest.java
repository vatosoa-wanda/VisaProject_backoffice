// package com.visa.backoffice.service;

// import com.visa.backoffice.dto.DocumentDTO;
// import com.visa.backoffice.exception.DemandeVerrouilleException;
// import com.visa.backoffice.exception.ResourceNotFoundException;
// import com.visa.backoffice.mapper.DocumentMapper;
// import com.visa.backoffice.model.*;
// import com.visa.backoffice.repository.DocumentRepository;
// import com.visa.backoffice.repository.DemandeRepository;
// import com.visa.backoffice.repository.TypePieceRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.web.multipart.MultipartFile;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class DocumentServiceTest {

//     @Mock
//     private DocumentRepository documentRepository;

//     @Mock
//     private DemandeRepository demandeRepository;

//     @Mock
//     private TypePieceRepository typePieceRepository;

//     @Mock
//     private DocumentMapper documentMapper;

//     @InjectMocks
//     private DocumentService documentService;

//     private Demande demandeCreee;
//     private Demande demandeScanTermine;
//     private TypePiece typePiece;
//     private Document document;

//     @BeforeEach
//     void setUp() {
//         // Préparer les données de test
//         StatutDemande statutCree = new StatutDemande();
//         statutCree.setId(1L);
//         statutCree.setLibelle("CREE");

//         StatutDemande statutTermine = new StatutDemande();
//         statutTermine.setId(2L);
//         statutTermine.setLibelle("SCAN_TERMINE");

//         demandeCreee = new Demande();
//         demandeCreee.setId(1L);
//         demandeCreee.setStatutDemande(statutCree);

//         demandeScanTermine = new Demande();
//         demandeScanTermine.setId(2L);
//         demandeScanTermine.setStatutDemande(statutTermine);

//         typePiece = new TypePiece();
//         typePiece.setId(1L);
//         typePiece.setCode("PIECE_IDENTITE");

//         document = new Document();
//         document.setId(1L);
//         document.setDemande(demandeCreee);
//         document.setTypePiece(typePiece);
//         document.setNomOriginal("test.pdf");
//         document.setCheminFichier("uploads/documents/1_1_123456_test.pdf");
//         document.setDateCreation(LocalDateTime.now());
//     }

//     @Test
//     void testGetDocumentsByDemande_Success() {
//         // Arrange
//         Long demandeId = 1L;
//         List<Document> documents = new ArrayList<>();
//         documents.add(document);

//         when(demandeRepository.existsById(demandeId)).thenReturn(true);
//         when(documentRepository.findByDemandeId(demandeId)).thenReturn(documents);
//         when(documentMapper.toDTO(any(Document.class))).thenReturn(new DocumentDTO());

//         // Act
//         List<DocumentDTO> result = documentService.getDocumentsByDemande(demandeId);

//         // Assert
//         assertNotNull(result);
//         assertEquals(1, result.size());
//         verify(documentRepository, times(1)).findByDemandeId(demandeId);
//     }

//     @Test
//     void testGetDocumentsByDemande_DemandeNotFound() {
//         // Arrange
//         Long demandeId = 999L;
//         when(demandeRepository.existsById(demandeId)).thenReturn(false);

//         // Act & Assert
//         assertThrows(ResourceNotFoundException.class,
//             () -> documentService.getDocumentsByDemande(demandeId));
//     }

//     @Test
//     void testSupprimerDocument_Success() {
//         // Arrange
//         Long docId = 1L;
//         when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

//         // Act
//         documentService.supprimerDocument(docId);

//         // Assert
//         verify(documentRepository, times(1)).delete(document);
//     }

//     @Test
//     void testSupprimerDocument_DemandeVerrouille() {
//         // Arrange
//         Long docId = 1L;
//         Document docTermine = new Document();
//         docTermine.setId(docId);
//         docTermine.setDemande(demandeScanTermine);

//         when(documentRepository.findById(docId)).thenReturn(Optional.of(docTermine));

//         // Act & Assert
//         assertThrows(DemandeVerrouilleException.class,
//             () -> documentService.supprimerDocument(docId));
//     }

//     @Test
//     void testSupprimerDocument_NotFound() {
//         // Arrange
//         Long docId = 999L;
//         when(documentRepository.findById(docId)).thenReturn(Optional.empty());

//         // Act & Assert
//         assertThrows(ResourceNotFoundException.class,
//             () -> documentService.supprimerDocument(docId));
//     }

//     @Test
//     void testDetermineContentType() {
//         // Test PDF
//         assertEquals("application/pdf", documentService.determineContentType("document.pdf"));

//         // Test JPG
//         assertEquals("image/jpeg", documentService.determineContentType("photo.jpg"));

//         // Test PNG
//         assertEquals("image/png", documentService.determineContentType("image.png"));

//         // Test default
//         assertEquals("application/octet-stream", documentService.determineContentType("unknown.xyz"));
//     }
// }
